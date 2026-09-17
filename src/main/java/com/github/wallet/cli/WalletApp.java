package com.github.wallet.cli;

import com.github.wallet.application.usecase.CloseAccountUseCase;
import com.github.wallet.application.usecase.CreateAccountUseCase;
import com.github.wallet.application.usecase.DepositMoneyUseCase;
import com.github.wallet.application.usecase.GetBalanceUseCase;
import com.github.wallet.application.usecase.GetTransactionHistoryUseCase;
import com.github.wallet.application.usecase.ListAccountsUseCase;
import com.github.wallet.application.usecase.TransferMoneyUseCase;
import com.github.wallet.application.usecase.WithdrawMoneyUseCase;
import com.github.wallet.cli.formatter.OutputFormatter;
import com.github.wallet.domain.service.FraudDetectionService;
import com.github.wallet.infrastructure.config.WalletConfiguration;
import com.github.wallet.infrastructure.event.LoggingEventSubscriber;
import com.github.wallet.infrastructure.event.SimpleEventPublisher;
import com.github.wallet.infrastructure.persistence.InMemoryAccountRepository;
import com.github.wallet.infrastructure.persistence.InMemoryTransactionRepository;
import com.github.wallet.infrastructure.persistence.JsonFileStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * Main application entry point — Composition Root.
 * Wires together all dependencies and starts the interactive CLI loop.
 */
public final class WalletApp {

    private static final Logger logger = LoggerFactory.getLogger(WalletApp.class);
    private static final String PROMPT = "wallet> ";

    private final CommandRouter commandRouter;
    private final JsonFileStorage jsonFileStorage;
    private final InMemoryAccountRepository accountRepository;
    private final InMemoryTransactionRepository transactionRepository;
    private final WalletConfiguration configuration;
    private final Scanner scanner;
    private final PrintStream output;

    /**
     * Create application with default config (stdin/stdout).
     */
    public WalletApp() {
        this(WalletConfiguration.defaultConfig(), new Scanner(System.in), System.out);
    }

    /**
     * Create application with custom config (for testing).
     */
    public WalletApp(final WalletConfiguration configuration, final Scanner scanner, final PrintStream output) {
        this.configuration = configuration;
        this.scanner = scanner;
        this.output = output;

        // Infrastructure
        this.accountRepository = new InMemoryAccountRepository();
        this.transactionRepository = new InMemoryTransactionRepository();
        this.jsonFileStorage = new JsonFileStorage(configuration.getDataDirectory());

        final SimpleEventPublisher eventPublisher = new SimpleEventPublisher();
        final LoggingEventSubscriber loggingSubscriber = new LoggingEventSubscriber();
        eventPublisher.subscribe(loggingSubscriber);

        // Domain services
        final FraudDetectionService fraudDetectionService =
                new FraudDetectionService(configuration.getSingleTransactionLimit());

        // Use cases
        final CreateAccountUseCase createAccount = new CreateAccountUseCase(accountRepository, eventPublisher);
        final DepositMoneyUseCase deposit = new DepositMoneyUseCase(accountRepository, transactionRepository, eventPublisher, fraudDetectionService);
        final WithdrawMoneyUseCase withdraw = new WithdrawMoneyUseCase(accountRepository, transactionRepository, eventPublisher, fraudDetectionService);
        final TransferMoneyUseCase transfer = new TransferMoneyUseCase(accountRepository, transactionRepository, eventPublisher, fraudDetectionService);
        final GetBalanceUseCase balance = new GetBalanceUseCase(accountRepository);
        final GetTransactionHistoryUseCase history = new GetTransactionHistoryUseCase(accountRepository, transactionRepository);
        final ListAccountsUseCase listAccounts = new ListAccountsUseCase(accountRepository);
        final CloseAccountUseCase closeAccount = new CloseAccountUseCase(accountRepository, eventPublisher);

        this.commandRouter = new CommandRouter(createAccount, deposit, withdraw, transfer, balance, history, listAccounts, closeAccount);

        // Load persisted data
        if (configuration.isPersistenceEnabled()) {
            loadPersistedData();
        }
    }

    /**
     * Start the interactive CLI loop.
     */
    public void run() {
        output.println(OutputFormatter.welcomeBanner());
        logger.info("Simple Wallet started with config: {}", configuration);

        while (true) {
            output.print(PROMPT);
            output.flush();

            if (!scanner.hasNextLine()) {
                break;
            }

            final String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }

            final String result = commandRouter.route(input);

            if (result == null) {
                // Exit command
                persistData();
                output.println("  Goodbye! Data saved.");
                logger.info("Simple Wallet shutting down.");
                break;
            }

            if (!result.isEmpty()) {
                output.println(result);
            }

            // Auto-persist after each command
            if (configuration.isPersistenceEnabled()) {
                persistData();
            }
        }
    }

    /**
     * Get the command router (for testing).
     */
    public CommandRouter getCommandRouter() {
        return commandRouter;
    }

    private void loadPersistedData() {
        try {
            jsonFileStorage.loadAccounts().forEach(accountRepository::save);
            jsonFileStorage.loadTransactions().forEach(transactionRepository::save);
            logger.info("Loaded {} accounts and {} transactions from disk",
                    accountRepository.count(), transactionRepository.count());
        } catch (final Exception e) {
            logger.warn("Could not load persisted data: {}. Starting fresh.", e.getMessage());
        }
    }

    private void persistData() {
        try {
            jsonFileStorage.saveAccounts(accountRepository.findAll());
            jsonFileStorage.saveTransactions(transactionRepository.findAll());
        } catch (final Exception e) {
            logger.error("Failed to persist data: {}", e.getMessage(), e);
        }
    }

    public static void main(final String[] args) {
        new WalletApp().run();
    }
}
