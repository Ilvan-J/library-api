package com.ilvan.libraryapi.service.impl;

import com.ilvan.libraryapi.exception.BusinessException;
import com.ilvan.libraryapi.model.entity.Loan;
import com.ilvan.libraryapi.model.repository.LoanRepository;
import com.ilvan.libraryapi.service.LoanService;

public class LoanServiceImpl implements LoanService {
    private LoanRepository repository;

    public LoanServiceImpl(LoanRepository repository) {
        this.repository = repository;
    }

    @Override
    public Loan save(Loan loan) {
        if ( repository.existsByBookAndNotReturned(loan.getBook()) ) {
            throw new BusinessException("Book already loaned");
        }
        return repository.save(loan);
    }
}
