package com.example.ShoppingService.services;

import com.example.ShoppingService.exceptions.InvalidISBNException;
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

    public ResponseEntity<Integer> getNumberOfBooksByISBN(String isbn) {
        try {
            return httpRequestHelper.get(
                    new StringBuilder(stockUrl).append("/book/").append(isbn).append("/stock").toString(),
                    Integer.class,
                    null
            );
        }
        catch (WebClientResponseException ex){
            return ResponseEntity.status(ex.getStatusCode()).build();
        }
    }

    public ResponseEntity<Void> orderBook(String isbn, Integer quantity) {
        ResponseEntity<Integer> responseNumberOfBooks = getNumberOfBooksByISBN(isbn);
        Integer numberOfBooks = responseNumberOfBooks.getBody();

        if(numberOfBooks!= null) {
            if(numberOfBooks < quantity){
                ResponseEntity<Void> responseOrder = orderProcess(isbn, quantity, numberOfBooks);
                if (responseOrder != null) return responseOrder;
            }
            ResponseEntity<Void> responseStockRemove = httpRequestHelper.patch(
                    new StringBuilder(stockUrl).append("/book/").append(isbn).append("/quantity/remove").toString(),
                    Void.class,
                    new HashMap<>() {{
                        put("quantity", quantity);
                    }}
            );
            return ResponseEntity.status(responseStockRemove.getStatusCode()).build();
        }
        else {
            return ResponseEntity.status(responseNumberOfBooks.getStatusCode()).build();
        }
    }

    private ResponseEntity<Void> orderProcess(String isbn, Integer quantity, Integer numberOfBooks) {
        int missingBooks = quantity - numberOfBooks;
        try {
            while (missingBooks != 0) {
                int missingBooksToOrder =
                        missingBooks > wholeSealerMaxQuantity ?
                                wholeSealerMaxQuantity :
                                missingBooks;

                ResponseEntity<Void> responseWholeSealerOrder = httpRequestHelper.post(
                        new StringBuilder(wholeSealerUrl).append("/book/order/").append(isbn).toString(),
                        Void.class,
                        new HashMap<>() {{
                            put("quantity", wholeSealerMaxQuantity);
                        }}
                );

                if (responseWholeSealerOrder.getStatusCode().is2xxSuccessful()) {
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
                        return ResponseEntity.status(responseStockAdd.getStatusCode()).build();
                    }
                } else {
                    return ResponseEntity.status(responseWholeSealerOrder.getStatusCode()).build();
                }
            }
        }
        catch (WebClientResponseException ex){
            return ResponseEntity.status(ex.getStatusCode()).build();
        }
        return null;
    }
}
