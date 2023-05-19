package com.example.ShoppingService.controllers;

import com.example.ShoppingService.exceptions.InvalidISBNException;
import com.example.ShoppingService.models.Book;
import com.example.ShoppingService.services.BookService;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("book")
@Slf4j
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping("/{ISBN}/stock")
    public ResponseEntity<Integer> getNumberOfBooksByISBN(@PathVariable("ISBN") String isbn){
        ResponseEntity<Integer> responseEntity = bookService.getNumberOfBooksByISBN(isbn);
        return ResponseEntity.status(responseEntity.getStatusCode()).body(responseEntity.getBody());
    }

    @PatchMapping("/{ISBN}/order")
    public ResponseEntity<Void> orderBook(@PathVariable("ISBN") String isbn, @RequestParam("quantity") Integer quantity){
        ResponseEntity<Void> responseEntity = bookService.orderBook(isbn, quantity);
        return ResponseEntity.status(responseEntity.getStatusCode()).build();
    }
}
