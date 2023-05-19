package com.example.ShoppingService.services;

import com.example.ShoppingService.exceptions.InvalidISBNException;
import com.example.ShoppingService.models.Account;
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

    public ResponseEntity<Integer> getNumberOfBooksByISBN(String isbn, Account account) {
        try {
            ResponseEntity<Integer> responseStock = httpRequestHelper.get(
                    new StringBuilder(stockUrl).append("/book/").append(isbn).append("/stock").toString(),
                    Integer.class,
                    null
            );
            log.info("{} books of \"{}\" have been found", responseStock.getBody(), isbn);
            return responseStock;
        }
        catch (WebClientResponseException ex){
            log.error("stock service returned a {} error code during the book count retrieval process", ex.getStatusCode());
            return ResponseEntity.status(ex.getStatusCode()).build();
        }
    }

    public ResponseEntity<Void> orderBook(String isbn, Integer quantity, Account account) {
        ResponseEntity<Integer> responseNumberOfBooks = getNumberOfBooksByISBN(isbn, account);
        Integer numberOfBooks = responseNumberOfBooks.getBody();

        if(numberOfBooks!= null) {
            if(numberOfBooks < quantity){
                ResponseEntity<Void> responseOrder = orderProcess(isbn, quantity, numberOfBooks, account);
                if (responseOrder != null) return responseOrder;
            }
            try {
                log.info("Recovery of {} books of \"{}\" in stock", quantity, isbn);
                ResponseEntity<Void> responseStockRemove = httpRequestHelper.patch(
                        new StringBuilder(stockUrl).append("/book/").append(isbn).append("/quantity/remove").toString(),
                        Void.class,
                        new HashMap<>() {{
                            put("quantity", quantity);
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

    private ResponseEntity<Void> orderProcess(String isbn, Integer quantity, Integer numberOfBooks, Account account) {
        int missingBooks = quantity - numberOfBooks;
        log.info("order process launched, {} books have been found but {} are required", numberOfBooks, quantity);
        try {
            while (missingBooks != 0) {
                int missingBooksToOrder =
                        missingBooks > wholeSealerMaxQuantity ?
                                wholeSealerMaxQuantity :
                                missingBooks;

                log.info("launching the order of {} books of \"{}\"", missingBooksToOrder, isbn);
                ResponseEntity<Void> responseWholeSealerOrder = httpRequestHelper.post(
                        new StringBuilder(wholeSealerUrl).append("/book/").append(isbn).append("/order").toString(),
                        Void.class,
                        new HashMap<>() {{
                            put("quantity", wholeSealerMaxQuantity);
                        }}
                );

                if (responseWholeSealerOrder.getStatusCode().is2xxSuccessful()) {
                    log.info("addition of {} books of \"{}\" in stock", missingBooksToOrder, isbn);
                    ResponseEntity<Void> responseStockAdd = httpRequestHelper.patch(
                            new StringBuilder(stockUrl).append("/book/").append(isbn).append("/quantity/add").toString(),
                            Void.class,
                            new HashMap<>() {{
                                put("quantity", missingBooksToOrder);
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
