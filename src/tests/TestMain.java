package tests;

import tests.calculator.Calculator;
import tests.lottery.Lottery;

public class TestMain {

	public static void main(String[] args) {
		new Calculator().createAndShowCalculator();
		new Lottery().createAndShowLottery();
	}

}