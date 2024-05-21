package com.game.controller;

import com.game.service.AccountService;
import com.game.service.GameService;
import com.game.util.InputHandler;
import com.game.util.RulePrinter;
import com.game.vo.CheckAccount;
import com.game.vo.FixedAccount;
import com.game.vo.SaveAccount;

import java.util.Random;
import java.util.Scanner;

public class GameController {
    private static final int END_DAY = 6;
    private static final int MIN_FEE = 300;
    private static final int MAX_FEE = 600;
    private static final int INITAIL_MONEY = 1000;
    private static final int SAVING_WITHDRAWAL_FEE = 500;
    private static int amount = 0; // 소지 금액

    public void startGame() {
        Scanner sc = new Scanner(System.in);
        AccountService accountService = new AccountService();
        GameService gameService = new GameService();
        InputHandler inputHandler = new InputHandler();
        RulePrinter rulePrinter = new RulePrinter();
        Random random = new Random();

        String name = InputHandler.getUserName(sc);
        String acNum = accountService.generateAccountNumber();


        CheckAccount checkAccount = new CheckAccount(name, acNum, 0);
        SaveAccount saveAccount = new SaveAccount(name, acNum, 0);

        int period = inputHandler.getPeriodFromUser(sc, 1, END_DAY);
        FixedAccount fixedAccount = new FixedAccount(name, acNum, 0, period);

        accountService.printAccountStates(checkAccount, saveAccount, fixedAccount);

        rulePrinter.printRule(sc);


        playGame(sc, random, checkAccount, saveAccount, fixedAccount, accountService, gameService, inputHandler);
    }

    private void playGame(Scanner sc, Random random, CheckAccount checkAccount, SaveAccount saveAccount, FixedAccount fixedAccount, AccountService accountService, GameService gameService, InputHandler inputHandler) {
        boolean stopFixedAccount = false;

        for (int i = 1; i <= END_DAY; i++) {
            System.out.println("--------------------------------------------------------------------------");
            System.out.println(i + "개월");
            int fee = random.nextInt((MAX_FEE - MIN_FEE) + 1) + MIN_FEE;

            if (i != 1) {
                gameService.handleMonthlyFee(checkAccount, fee);
            }

            if (i == fixedAccount.getPeriod()) {
                gameService.handleExpired(checkAccount, fixedAccount);
                stopFixedAccount = true;
            }

            gameService.handleSavingInterest(saveAccount);

            amount = INITAIL_MONEY;
            int stopCheck = 0;
            do {
                System.out.println("소지 잔액 : " + amount + "(소지 잔액은 다음달이 지나면 사라집니다.)");
                System.out.println("원하는 행동을 선택해주세요.");
                printMenu();

                int action = InputHandler.getUserChoice(sc, 4);

                if (handleUserAction(sc, action, checkAccount, saveAccount, fixedAccount, stopFixedAccount, inputHandler, accountService)) {
                    stopCheck = 1;
                } else {
                    stopCheck = 2;
                }
            } while(stopCheck == 2);
        }
        int resultMoney = checkAccount.getBalance() + saveAccount.getBalance() + fixedAccount.getBalance();
        System.out.println("6개월 동안 총 " + resultMoney + "원을 모으셨습니다.");
    }

    private boolean handleUserAction(Scanner sc, int action, CheckAccount checkAccount, SaveAccount saveAccount, FixedAccount fixedAccount, boolean stopFixedAccount, InputHandler inputHandler, AccountService accountService) {
        return switch (action) {
            case 1 -> {
                accountService.printAccountStates(checkAccount, saveAccount, fixedAccount);
                yield false;
            }
            case 2 -> {
                amount -= accountService.depositChoice(sc, checkAccount, saveAccount, fixedAccount, inputHandler, amount, stopFixedAccount);
                yield false;
            }
            case 3 -> {
                amount += accountService.withdrawalChoice(sc, checkAccount, saveAccount, fixedAccount, inputHandler, SAVING_WITHDRAWAL_FEE);
                yield false;
            }
            case 4 -> {
                System.out.println("행동 종료(다음달로 넘어갑니다.)");
                yield true;
            }
            default -> {
                System.out.println("다시 입력해주세요");
                yield false;
            }
        };
    }

    private void printMenu() {
        System.out.println("1. 계좌 정보");
        System.out.println("2. 입금");
        System.out.println("3. 출금");
        System.out.println("4. 행동 종료");
    }
}
