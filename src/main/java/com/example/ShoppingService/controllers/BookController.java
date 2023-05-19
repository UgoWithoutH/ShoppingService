package com.example.ShoppingService.controllers;

import com.example.ShoppingService.models.Account;
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
    public ResponseEntity<Integer> getNumberOfBooksByISBN(@PathVariable("ISBN") String isbn, @RequestBody Account account){
        log.info("{} - {} : Get request received from account \"{}\" to get number of books of \"{}\"", account.getLogin(), isbn, account.getLogin(), isbn);
        ResponseEntity<Integer> responseEntity = bookService.getNumberOfBooksByISBN(isbn, account);
        return ResponseEntity.status(responseEntity.getStatusCode()).body(responseEntity.getBody());
    }

    @PatchMapping("/{ISBN}/order")
    public ResponseEntity<Void> orderBook(@PathVariable("ISBN") String isbn, @RequestParam("quantity") Integer quantity, @RequestBody Account account){
        log.info("{} - {} : Patch request received from account \"{}\" to order {} books of \"{}\"", account.getLogin(), isbn, account.getLogin(), quantity, isbn);
        ResponseEntity<Void> responseEntity = bookService.orderBook(isbn, quantity, account);
        return ResponseEntity.status(responseEntity.getStatusCode()).build();
    }
}
