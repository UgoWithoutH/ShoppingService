package com.example.ShoppingService.controllers;

import com.example.ShoppingService.services.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("book")
@Slf4j
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping("/{ISBN}/stock")
    public ResponseEntity<Integer> getNumberOfBooksByISBN(@PathVariable("ISBN") String isbn, @RequestParam String accountLogin){
        log.info("{} - {} : Get request received from account \"{}\" to get number of books \"{}\"", accountLogin, isbn, accountLogin, isbn);
        ResponseEntity<Integer> responseEntity = bookService.getNumberOfBooksByISBN(isbn, accountLogin);
        return ResponseEntity.status(responseEntity.getStatusCode()).body(responseEntity.getBody());
    }

    @PatchMapping("/{ISBN}/order")
    public ResponseEntity<Void> orderBook(@PathVariable("ISBN") String isbn, @RequestParam("quantity") Integer quantity, @RequestParam String accountLogin){
        log.info("{} - {} : Patch request received from account \"{}\" to order {} books \"{}\"", accountLogin, isbn, accountLogin, quantity, isbn);
        ResponseEntity<Void> responseEntity = bookService.orderBook(isbn, quantity, accountLogin);
        return ResponseEntity.status(responseEntity.getStatusCode()).build();
    }
}
