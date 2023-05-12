package com.ilvan.libraryapi.api.resource;

import com.ilvan.libraryapi.api.dto.BookDTO;
import com.ilvan.libraryapi.api.dto.LoanDTO;
import com.ilvan.libraryapi.model.entity.Book;
import com.ilvan.libraryapi.model.entity.Loan;
import com.ilvan.libraryapi.service.BookService;
import com.ilvan.libraryapi.service.LoanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Api("Book API")
@Slf4j
public class BookController {

    private final BookService service;
    private final ModelMapper modelMapper;
    private final LoanService loanService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Creates a book")
    @ApiResponses(
            @ApiResponse(code = 201, message = "Book succesfully created")
    )
    public BookDTO create(@RequestBody @Valid BookDTO dto) {
        log.info(" creating a book for isbn: {} ", BookDTO.class);
        Book entity = modelMapper.map(dto, Book.class);
        entity = service.save(entity);
        return modelMapper.map(entity, BookDTO.class);
    }

    @GetMapping("{id}")
    @ApiOperation("Obtains a book details by id")
    @ApiResponses(
            @ApiResponse(code = 404, message = "Book not found")
    )
    public BookDTO get(@PathVariable Long id) {
        log.info(" obtaining details for book id: {} ", id);
        return service
                .getById(id)
                .map( book -> modelMapper.map(book, BookDTO.class) )
                .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND) );
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation("Deletes a book by Id")
    @ApiResponses(
            @ApiResponse(code = 204, message = "Book succesfully deleted")
    )
    public void delete(@PathVariable Long id) {
        log.info(" deleting book of id: {} ", id);
        Book book = service.getById(id).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND) );
        service.delete(book);
    }

    @PutMapping("{id}")
    @ApiOperation("Uptates a book")
    @ApiResponses(
            @ApiResponse(code = 404, message = "Book not found")
    )
    public BookDTO update( @PathVariable Long id, @RequestBody @Valid BookDTO dto) {
        log.info(" updating book of id: {} ", id);
       return service.getById(id).map( book -> {

           book.setAuthor(dto.getAuthor());
           book.setIsbn(dto.getTitle());
           book = service.update(book);
           return modelMapper.map(book, BookDTO.class);

       } ).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND) );

    }

    @GetMapping
    @ApiOperation("Find books by params")
    @ApiResponses(
            @ApiResponse(code = 404, message = "Book not found")
    )
    public Page<BookDTO> find(BookDTO dto, Pageable pageRequest) {
        Book filter = modelMapper.map(dto, Book.class);
        Page<Book> result = service.find(filter, pageRequest);
        List<BookDTO> list = result.getContent()
                .stream()
                .map(entity -> modelMapper.map(entity, BookDTO.class))
                .collect(Collectors.toList());
        return new PageImpl<BookDTO>( list, pageRequest, result.getTotalElements() );
    }

    @GetMapping("{id}/loans")
    @ApiOperation("Obtains a loans details by id")
    public Page<LoanDTO> loansByBook( @PathVariable Long id, Pageable pageable) {
        Book book = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Page<Loan> result = loanService.getLoanByBook(book, pageable);
        List<LoanDTO> list = result.getContent()
                .stream()
                .map(loan -> {

                    Book loanBook = loan.getBook();
                    BookDTO bookDTO = modelMapper.map(loanBook, BookDTO.class);
                    LoanDTO loanDTO = modelMapper.map(loan, LoanDTO.class);
                    loanDTO.setBookDTO(bookDTO);
                    return loanDTO;
                }).collect(Collectors.toList());
        return new PageImpl<LoanDTO>(list, pageable, result.getTotalElements());
    }
}
