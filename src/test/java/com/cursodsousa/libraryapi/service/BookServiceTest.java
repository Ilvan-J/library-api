package com.cursodsousa.libraryapi.service;

import com.cursodsousa.libraryapi.exception.BusinessException;
import com.cursodsousa.libraryapi.model.entity.Book;
import com.cursodsousa.libraryapi.model.repository.BookRepository;
import com.cursodsousa.libraryapi.service.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService service;
    @MockBean
    BookRepository repository;

    @BeforeEach
    public void setUp() {
        this.service = new BookServiceImpl( repository );
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest() {
        //cenário
        Book book = createValidBook();
        when( repository.existsByIsbn(Mockito.anyString()) ).thenReturn(false);
        when( repository.save(book) ).thenReturn(
                        Book.builder().id(1l)
                                .isbn("123")
                                .title("As aventuras")
                                .author("Fulano")
                                .build()
                );

        //execução
        Book savedBook = service.save(book);

        //verificação
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo("123");
        assertThat(savedBook.getTitle()).isEqualTo("As aventuras");
        assertThat(savedBook.getAuthor()).isEqualTo("Fulano");
    }

    @Test
    @DisplayName("Deve lançar erro de negocio ao tentar cadastrar um livro com um isbn duplicado")
    public void shouldNotSaveABookWithDuplicatedISBN() {
        //cenário
        Book book = createValidBook();
        when( repository.existsByIsbn(Mockito.anyString()) ).thenReturn(true);

        //execução
        Throwable exception = Assertions.catchThrowable(() -> service.save(book));

        //verificações
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Isbn já cadastrado.");

        verify(repository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve obter um livro por id.")
    public void getByIdTest() {
        Long id = 1l;
        Book book = createValidBook();
        book.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(book));

        //execução
        Optional<Book> foundBook = service.getById(id);

        //verificações

        assertThat( foundBook.isPresent() ).isTrue();
        assertThat( foundBook.get().getId()).isEqualTo(id);
        assertThat( foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat( foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
        assertThat( foundBook.get().getTitle()).isEqualTo(book.getTitle());
    }

    @Test
    @DisplayName("Deve retornar vazio ao obter um livro por Id quando ele não existe na base.")
    public void getBookNotFoundByIdTest() {
        Long id = 1l;
        when( repository.findById(id) ).thenReturn(Optional.empty());

        //execução
        Optional<Book> book = service.getById(id);

        //verificações

        assertThat( book.isPresent() ).isFalse();
    }

    @Test
    @DisplayName("Deve deletar um livro.")
    public void deleteBookTest() {
        //cenário
        Book book = Book.builder().id(1l).build();

        //execução
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> service.delete(book));

        //verificação
        verify(repository, times(1)).delete(book);
    }

    @Test
    @DisplayName("Deve ocorrer erro ao tentar deletar um livro inexistente.")
    public void deleteInvalidBookTest() {
        Book book = new Book();

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.delete(book));

        verify(repository, Mockito.never()).delete(book);
    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void bookUpdateTest() {
        //cenário
        Long id = 1l;

        //livro a atualizar
        Book updatingBook = Book.builder().id(id).build();

        //simulado
        Book updatedBook = createValidBook();
        updatedBook.setId(id);
        when( repository.save(updatingBook)).thenReturn(updatedBook);

        //execução

        Book book = service.update(updatingBook);

        //verificações

        assertThat(book.getId()).isEqualTo(updatingBook.getId() );
        assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
        assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());
        assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
    }

    @Test
    @DisplayName("Deve ocorrer erro ao tentar atualizar um livro inexistente.")
    public void updateInvaliupdTest() {
        Book book = new Book();

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.update(book));

        verify(repository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve filtar livros pelas propriedades")
    public void findBookTest() {

        //cenário
        Book book = createValidBook();
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Book> lista = Arrays.asList(book);
        Page<Book> page = new PageImpl<Book>(lista, pageRequest, 1);
        when( repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class)))
                .thenReturn(page);

        //execução
        Page<Book> result = service.find(book, pageRequest);

        //verificações
        assertThat( result.getTotalElements()).isEqualTo(1);
        assertThat( result.getContent()).isEqualTo(lista);
        assertThat( result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat( result.getPageable().getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Deve obter um livro pelo isbn")
    public void getBookByIsbnTest() {
        String isbn = "1230";
        when(repository.findByIsbn(isbn)).thenReturn( Optional.of(Book.builder().id(1l).isbn(isbn).build()) );

        Optional<Book> book = service.getBookByIsbn(isbn);
        assertThat(book.isPresent()).isTrue();
        assertThat(book.get().getId()).isEqualTo(1);
        assertThat(book.get().getIsbn()).isEqualTo(isbn);

        verify(repository, times(1)).findByIsbn(isbn);
    }

    private Book createValidBook() {
        return Book.builder().isbn("123").author("Fulano").title("As aventuras").build();
    }

}
