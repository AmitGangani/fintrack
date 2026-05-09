package com.amit.fintrack.account.service;

import com.amit.fintrack.account.dto.AccountRequest;
import com.amit.fintrack.account.dto.AccountResponse;
import com.amit.fintrack.account.entity.Account;
import com.amit.fintrack.account.entity.AccountType;
import com.amit.fintrack.account.exception.AccountNotFoundException;
import com.amit.fintrack.account.repository.AccountRepository;
import com.amit.fintrack.account.security.CurrentUserService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountServiceTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    void createAccountStoresCurrentUserAndUppercaseCurrency() {
        FakeAccountRepository accountRepository = new FakeAccountRepository();
        AccountService accountService = new AccountService(
                accountRepository.repository(),
                new FixedCurrentUserService()
        );

        AccountResponse response = accountService.createAccount(new AccountRequest(
                "Wallet",
                AccountType.WALLET,
                new BigDecimal("500.00"),
                "inr"
        ));

        Account savedAccount = accountRepository.savedAccount;
        assertEquals(USER_ID, savedAccount.getUserId());
        assertEquals("INR", savedAccount.getCurrency());
        assertEquals(ACCOUNT_ID, response.id());
        assertEquals("INR", response.currency());
    }

    @Test
    void adjustBalanceAddsAmountChange() {
        FakeAccountRepository accountRepository = new FakeAccountRepository();
        accountRepository.account = account(new BigDecimal("100.00"));
        AccountService accountService = new AccountService(
                accountRepository.repository(),
                new FixedCurrentUserService()
        );

        AccountResponse response = accountService.adjustBalance(ACCOUNT_ID, new BigDecimal("25.50"));

        assertEquals(new BigDecimal("125.50"), response.balance());
        assertEquals(1, accountRepository.saveCount);
    }

    @Test
    void updateAccountUpdatesFieldsAndUppercaseCurrency() {
        FakeAccountRepository accountRepository = new FakeAccountRepository();
        accountRepository.account = account(new BigDecimal("100.00"));
        AccountService accountService = new AccountService(
                accountRepository.repository(),
                new FixedCurrentUserService()
        );

        AccountResponse response = accountService.updateAccount(
                ACCOUNT_ID,
                new AccountRequest("Main Bank", AccountType.BANK, new BigDecimal("250.00"), "usd")
        );

        assertEquals("Main Bank", response.name());
        assertEquals(AccountType.BANK, response.type());
        assertEquals(new BigDecimal("250.00"), response.balance());
        assertEquals("USD", response.currency());
        assertEquals(1, accountRepository.saveCount);
    }

    @Test
    void deleteAccountDeletesOwnedAccount() {
        FakeAccountRepository accountRepository = new FakeAccountRepository();
        accountRepository.account = account(new BigDecimal("100.00"));
        AccountService accountService = new AccountService(
                accountRepository.repository(),
                new FixedCurrentUserService()
        );

        accountService.deleteAccount(ACCOUNT_ID);

        assertEquals(1, accountRepository.deleteCount);
    }

    @Test
    void getAccountByIdThrowsWhenAccountDoesNotBelongToUser() {
        AccountService accountService = new AccountService(
                new FakeAccountRepository().repository(),
                new FixedCurrentUserService()
        );

        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountById(ACCOUNT_ID));
    }

    private static Account account(BigDecimal balance) {
        return Account.builder()
                .id(ACCOUNT_ID)
                .userId(USER_ID)
                .name("Wallet")
                .type(AccountType.WALLET)
                .balance(balance)
                .currency("INR")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private static final class FakeAccountRepository {
        private Account account;
        private Account savedAccount;
        private int saveCount;
        private int deleteCount;

        private AccountRepository repository() {
            return (AccountRepository) Proxy.newProxyInstance(
                    AccountRepository.class.getClassLoader(),
                    new Class<?>[]{AccountRepository.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "findByIdAndUserId" -> Optional.ofNullable(account);
                        case "save" -> {
                            Account saved = (Account) args[0];
                            saved.setId(ACCOUNT_ID);
                            savedAccount = saved;
                            account = saved;
                            saveCount++;
                            yield saved;
                        }
                        case "delete" -> {
                            deleteCount++;
                            yield null;
                        }
                        case "toString" -> "FakeAccountRepository";
                        default -> throw new UnsupportedOperationException(method.getName());
                    }
            );
        }
    }

    private static final class FixedCurrentUserService extends CurrentUserService {
        @Override
        public UUID getCurrentUserId() {
            return USER_ID;
        }
    }
}
