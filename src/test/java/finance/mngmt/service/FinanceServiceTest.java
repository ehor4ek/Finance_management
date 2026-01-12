package finance.mngmt.service;

import finance.mngmt.model.*;
import finance.mngmt.repository.UserRepository;
import finance.mngmt.exception.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FinanceServiceTest {
    private UserRepository userRepository;
    private UserService userService;
    private AlertService alertService;
    private FinanceService financeService;
    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = mock(UserService.class);
        alertService = mock(AlertService.class);
        financeService = new FinanceService(userService, alertService);

        testUser = new User("testuser", "password");
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(userService.isAuthenticated()).thenReturn(true);
    }

    @Test
    void testAddIncome() {
        financeService.addIncome(1000.0, "Зарплата", "Оклад");

        assertEquals(1, testUser.getWallet().getTransactions().size());
        Transaction transaction = testUser.getWallet().getTransactions().get(0);
        assertEquals(1000.0, transaction.getAmount());
        assertEquals(TransactionType.INCOME, transaction.getType());
        assertEquals("Зарплата", transaction.getCategory());
        assertEquals("Оклад", transaction.getDescription());

        verify(alertService).checkBalanceAlerts(any(Wallet.class));
    }

    @Test
    void testAddExpense() {
        financeService.addExpense(500.0, "Еда", "Обед");

        assertEquals(1, testUser.getWallet().getTransactions().size());
        Transaction transaction = testUser.getWallet().getTransactions().get(0);
        assertEquals(500.0, transaction.getAmount());
        assertEquals(TransactionType.EXPENSE, transaction.getType());
        assertEquals("Еда", transaction.getCategory());

        verify(alertService).checkBudgetAlerts(any(Wallet.class));
        verify(alertService).checkBalanceAlerts(any(Wallet.class));
    }

    @Test
    void testAddIncomeWithNegativeAmount() {
        assertThrows(ValidationException.class, () -> {
            financeService.addIncome(-100.0, "Категория", "");
        });
    }

    @Test
    void testAddExpenseWithZeroAmount() {
        assertThrows(ValidationException.class, () -> {
            financeService.addExpense(0, "Категория", "");
        });
    }

    @Test
    void testAddCategory() {
        financeService.addCategory("Новая категория");
        assertTrue(testUser.getWallet().getCategories().contains("Новая категория"));
    }

    @Test
    void testAddEmptyCategory() {
        assertThrows(ValidationException.class, () -> {
            financeService.addCategory("");
        });
    }

    @Test
    void testSetBudget() {
        financeService.setBudget("Еда", 5000.0);
        assertTrue(testUser.getWallet().getBudgets().containsKey("Еда"));
        assertEquals(5000.0, testUser.getWallet().getBudgets().get("Еда").getLimit());
    }

    @Test
    void testSetBudgetWithNegativeLimit() {
        assertThrows(ValidationException.class, () -> {
            financeService.setBudget("Еда", -100.0);
        });
    }

    @Test
    void testEditBudget() {
        financeService.setBudget("Еда", 5000.0);
        financeService.editBudget("Еда", 6000.0);

        assertEquals(6000.0, testUser.getWallet().getBudgets().get("Еда").getLimit());
    }

    @Test
    void testEditNonExistentBudget() {
        assertThrows(CategoryNotFoundException.class, () -> {
            financeService.editBudget("Несуществующая", 1000.0);
        });
    }
}