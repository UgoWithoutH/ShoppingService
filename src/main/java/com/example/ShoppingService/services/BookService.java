package com.example.ShoppingService.services;

import com.example.ShoppingService.utils.HttpRequestHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;

@Service
@Slf4j
public class BookService {

    @Autowired
    private HttpRequestHelper httpRequestHelper;

    @Value("${service.stockService.url}")
    private String stockUrl;

    @Value("${service.wholeSealerService.url}")
    private String wholeSealerUrl;

    @Value("${service.wholeSealerService.maxQuantity}")
    private Integer wholeSealerMaxQuantity;

    public ResponseEntity<Integer> getNumberOfBooksByISBN(String isbn, String accountLogin) {
        try {
            ResponseEntity<Integer> responseStock = httpRequestHelper.get(
                    new StringBuilder(stockUrl).append("/book/").append(isbn).append("/stock").toString(),
                    Integer.class,
                    new HashMap<>() {{
                        put("accountLogin", accountLogin);
                    }}
            );
            log.info("{} books \"{}\" have been found", responseStock.getBody(), isbn);
            return responseStock;
        }
        catch (WebClientResponseException ex){
            log.error("stock service returned a {} error code during the book count retrieval process", ex.getStatusCode());
            return ResponseEntity.status(ex.getStatusCode()).build();
        }
    }

    public ResponseEntity<Void> orderBook(String isbn, Integer quantity, String accountLogin) {
        ResponseEntity<Integer> responseNumberOfBooks = getNumberOfBooksByISBN(isbn, accountLogin);
        Integer numberOfBooks = responseNumberOfBooks.getBody();

        if(numberOfBooks!= null) {
            if(numberOfBooks < quantity){
                ResponseEntity<Void> responseOrder = orderProcess(isbn, quantity, numberOfBooks, accountLogin);
                if (responseOrder != null) return responseOrder;
            }
            try {
                log.info("Recovery of {} books \"{}\" in stock", quantity, isbn);
                ResponseEntity<Void> responseStockRemove = httpRequestHelper.patch(
                        new StringBuilder(stockUrl).append("/book/").append(isbn).append("/quantity/remove").toString(),
                        Void.class,
                        new HashMap<>() {{
                            put("quantity", quantity);
                            put("accountLogin", accountLogin);
                        }}
                );
                return ResponseEntity.status(responseStockRemove.getStatusCode()).build();
            }
            catch (WebClientResponseException ex){
                log.error("stock service returned a {} error code during the stock retrieval process", ex.getStatusCode());
                return ResponseEntity.status(ex.getStatusCode()).build();
            }
        }
        else {
            return ResponseEntity.status(responseNumberOfBooks.getStatusCode()).build();
        }
    }

    private ResponseEntity<Void> orderProcess(String isbn, Integer quantity, Integer numberOfBooks, String accountLogin) {
        int missingBooks = quantity - numberOfBooks;
        log.info("order process launched, {} books have been found but {} are required", numberOfBooks, quantity);
        try {
            while (missingBooks != 0) {
                int missingBooksToOrder =
                        missingBooks > wholeSealerMaxQuantity ?
                                wholeSealerMaxQuantity :
                                missingBooks;

                log.info("launching the order of {} books \"{}\"", missingBooksToOrder, isbn);
                ResponseEntity<Void> responseWholeSealerOrder = httpRequestHelper.post(
                        new StringBuilder(wholeSealerUrl).append("/book/").append(isbn).append("/order").toString(),
                        Void.class,
                        new HashMap<>() {{
                            put("quantity", wholeSealerMaxQuantity);
                            put("accountLogin", accountLogin);
                        }}
                );

                if (responseWholeSealerOrder.getStatusCode().is2xxSuccessful()) {
                    log.info("addition of {} books \"{}\" in stock", missingBooksToOrder, isbn);
                    ResponseEntity<Void> responseStockAdd = httpRequestHelper.patch(
                            new StringBuilder(stockUrl).append("/book/").append(isbn).append("/quantity/add").toString(),
                            Void.class,
                            new HashMap<>() {{
                                put("quantity", missingBooksToOrder);
                                put("accountLogin", accountLogin);
                            }}
                    );

                    if (responseStockAdd.getStatusCode().is2xxSuccessful()) {
                        missingBooks -= missingBooksToOrder;
                    } else {
                        log.error("stock service returned a {} error code", responseStockAdd.getStatusCode());
                        return ResponseEntity.status(responseStockAdd.getStatusCode()).build();
                    }
                } else {
                    log.error("whole sealer service returned a {} error code", responseWholeSealerOrder.getStatusCode());
                    return ResponseEntity.status(responseWholeSealerOrder.getStatusCode()).build();
                }
            }
        }
        catch (WebClientResponseException ex){
            log.error("an error code {} was returned during the order process", ex.getStatusCode());
            return ResponseEntity.status(ex.getStatusCode()).build();
        }
        return null;
    }
}
