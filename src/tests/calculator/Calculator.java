package tests.calculator;

import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Stack;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class Calculator {

	// 運算歷史
	private Stack<String> lastNum1Stack = new Stack<>();
	private Stack<String> lastNum2Stack = new Stack<>();
	private Stack<String> operatorStack = new Stack<>();
	private Stack<BigDecimal> undoStack = new Stack<>();
	private Stack<BigDecimal> redoStack = new Stack<>();
	private Stack<String> redoNum1Stack = new Stack<>();
	private Stack<String> redoNum2Stack = new Stack<>();
	private Stack<String> redoOperatorStack = new Stack<>();

	public void createAndShowCalculator() {
		// 欄位設定
		JFrame frame = new JFrame("計算機");
		frame.setSize(400, 250);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new GridLayout(7, 2));
		JLabel label1 = new JLabel("數字1: (僅能輸入數字)");
		JTextField text1 = new JTextField();
		JLabel label2 = new JLabel("數字2: (僅能輸入數字)");
		JTextField text2 = new JTextField();
		JLabel resultLabel = new JLabel("結果:");
		JTextField resultField = new JTextField();
		resultField.setEditable(false);

		JButton addButton = new JButton("+");
		JButton subButton = new JButton("-");
		JButton mulButton = new JButton("*");
		JButton divButton = new JButton("/");
		JButton undoButton = new JButton("Undo");
		JButton redoButton = new JButton("Redo");

		frame.add(label1);
		frame.add(text1);
		frame.add(label2);
		frame.add(text2);
		frame.add(addButton);
		frame.add(subButton);
		frame.add(mulButton);
		frame.add(divButton);
		frame.add(undoButton);
		frame.add(redoButton);
		frame.add(resultLabel);
		frame.add(resultField);

		// 只能輸入數字及一個小數點
		DocumentFilter numericFilter = new DocumentFilter() {
			@Override
			public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
					throws BadLocationException {
				if (string == null)
					return;
				String newText = fb.getDocument().getText(0, fb.getDocument().getLength()) + string;
				if (isValidDecimal(newText)) {
					super.insertString(fb, offset, string, attr);
				}
			}

			@Override
			public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
					throws BadLocationException {
				if (text == null)
					return;
				String oldText = fb.getDocument().getText(0, fb.getDocument().getLength());
				StringBuilder sb = new StringBuilder(oldText);
				sb.replace(offset, offset + length, text);
				if (isValidDecimal(sb.toString())) {
					super.replace(fb, offset, length, text, attrs);
				}
			}

			private boolean isValidDecimal(String text) {
				return text.isEmpty() || text.matches("\\d*\\.?\\d*");
			}
		};
		((AbstractDocument) text1.getDocument()).setDocumentFilter(numericFilter);
		((AbstractDocument) text2.getDocument()).setDocumentFilter(numericFilter);
		//運算按鈕監聽
		ActionListener calcListener = e -> {
			try {
				BigDecimal num1 = new BigDecimal(text1.getText());
				BigDecimal num2 = new BigDecimal(text2.getText());
				BigDecimal result = BigDecimal.ZERO;
				String op = "";

				if (e.getSource() == addButton) {
					result = num1.add(num2);
					op = "+";
				} else if (e.getSource() == subButton) {
					result = num1.subtract(num2);
					op = "-";
				} else if (e.getSource() == mulButton) {
					result = num1.multiply(num2);
					op = "*";
				} else if (e.getSource() == divButton) {
					if (num2.compareTo(BigDecimal.ZERO) == 0) {
						JOptionPane.showMessageDialog(frame, "除數不可為零！");
						return;
					}
					result = num1.divide(num2, 10, RoundingMode.HALF_UP);
					op = "/";
				}

				// 推入 undo
				undoStack.push(result);
				lastNum1Stack.push(num1.toPlainString());
				lastNum2Stack.push(num2.toPlainString());
				operatorStack.push(op);

				// 清空 redo
				redoStack.clear();
				redoNum1Stack.clear();
				redoNum2Stack.clear();
				redoOperatorStack.clear();

				// 更新顯示
				resultField.setText(num1.toPlainString() + op + num2.toPlainString() + "=" + result.toPlainString());
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(frame, "請輸入有效的數字！");
			}
		};
		//加入監聽
		addButton.addActionListener(calcListener);
		subButton.addActionListener(calcListener);
		mulButton.addActionListener(calcListener);
		divButton.addActionListener(calcListener);
		//undo加入監聽
		undoButton.addActionListener(e -> {
			if (!undoStack.isEmpty()) {
				//undo 堆 反回上一部
				BigDecimal lastResult = undoStack.pop();
				String lastNum1 = lastNum1Stack.pop();
				String lastNum2 = lastNum2Stack.pop();
				String lastOp = operatorStack.pop();
				//redoButton 堆 加入數值
				redoStack.push(lastResult);
				redoNum1Stack.push(lastNum1);
				redoNum2Stack.push(lastNum2);
				redoOperatorStack.push(lastOp);
				//undo 堆 顯示當前數據
 				if (!undoStack.isEmpty()) {
					resultField.setText(lastNum1Stack.peek() + operatorStack.peek() + lastNum2Stack.peek() + "="
							+ undoStack.peek().toPlainString());
					text1.setText(lastNum1Stack.peek());
					text2.setText(lastNum2Stack.peek());
				} else {
					resultField.setText("");
					text1.setText("");
					text2.setText("");
				}
			} else {
				JOptionPane.showMessageDialog(frame, "沒有可以復原的操作！");
			}
		});
		//redoButton加入監聽
		redoButton.addActionListener(e -> {
			if (!redoStack.isEmpty()) {
				//redoButton 堆 反回上一部
				BigDecimal redoResult = redoStack.pop();
				String redoNum1 = redoNum1Stack.pop();
				String redoNum2 = redoNum2Stack.pop();
				String redoOp = redoOperatorStack.pop();
				//undo 堆 加入數值
				undoStack.push(redoResult);
				lastNum1Stack.push(redoNum1);
				lastNum2Stack.push(redoNum2);
				operatorStack.push(redoOp);
				//redoButton 堆 顯示當前數據
				text1.setText(redoNum1);
				text2.setText(redoNum2);
				resultField.setText(redoNum1 + redoOp + redoNum2 + "=" + redoResult.toPlainString());
			} else {
				JOptionPane.showMessageDialog(frame, "沒有可以取消復原的操作！");
			}
		});

		frame.setVisible(true);
	}
}
