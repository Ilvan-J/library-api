package com.ilvan.libraryapi.service.impl;

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
        return repository.save(loan);
    }
}
