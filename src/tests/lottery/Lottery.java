package tests.lottery;

import java.awt.GridLayout;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import tests.lottery.dto.Prize;

public class Lottery {

	// 用戶抽獎記錄
	private Set<String> userDrawRecord = new HashSet<>();

	// 獎品清單
	private List<Prize> prizes = new ArrayList<>();

	// 隨機物件
	private SecureRandom secureRandom = new SecureRandom();

	public void createAndShowLottery() {
		// 初始化獎品
		prizes.add(new Prize("電動牙刷", 100, 30));
		prizes.add(new Prize("保溫杯", 300, 20));
		prizes.add(new Prize("紅包券", 200, 10));

		JFrame frame = new JFrame("電商抽獎系統");
		frame.setSize(450, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new GridLayout(6, 1, 5, 5));

		JTextField userIdField = new JTextField();
		JTextField drawCountField = new JTextField();
		// 限制輸入0~9五位數
		((AbstractDocument) drawCountField.getDocument()).setDocumentFilter(new DocumentFilter() {
			@Override
			public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
					throws BadLocationException {
				if (string == null)
					return;
				String newText = fb.getDocument().getText(0, fb.getDocument().getLength()) + string;
				if (newText.matches("\\d{0,5}")) {
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
				if (sb.toString().matches("\\d{0,5}")) {
					super.replace(fb, offset, length, text, attrs);
				}
			}
		});

		JTextArea resultArea = new JTextArea();
		resultArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(resultArea);

		JButton drawButton = new JButton("開始抽獎");

		// 加入元件
		frame.add(new JLabel("用戶ID："));
		frame.add(userIdField);
		frame.add(new JLabel("抽獎次數： (限制輸入5位數字)"));
		frame.add(drawCountField);
		frame.add(drawButton);
		frame.add(scrollPane);

		// 抽獎事件
		drawButton.addActionListener(e -> {
			String userId = userIdField.getText().trim();
			String countStr = drawCountField.getText().trim();

			if (userId.isEmpty()) {
				JOptionPane.showMessageDialog(frame, "請輸入用戶ID！");
				return;
			}

			int count;
			try {
				count = Integer.parseInt(countStr);
				if (count <= 0)
					throw new NumberFormatException();
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(frame, "請輸入正確的抽獎次數！");
				return;
			}

			// 抽獎
			List<String> results = drawLottery(userId, count);

			// 顯示結果
			resultArea.setText("");
			for (String res : results) {
				resultArea.append(res + "\n");
			}

			// 顯示剩餘庫存
			resultArea.append("\n--- 獎品庫存 ---\n");
			for (Prize p : prizes) {
				resultArea.append(p.name + " 剩餘：" + p.quantity + "\n");
			}
		});

		frame.setVisible(true);
	}

	/**
	 * 多次抽獎
	 */
	private List<String> drawLottery(String userId, int count) {
		List<String> results = new ArrayList<>();

		if (userDrawRecord.contains(userId)) {
			results.add("您已經參加過此次抽獎，不能重複抽獎！");
			return results;
		}

		userDrawRecord.add(userId);

		for (int i = 0; i < count; i++) {
			results.add(drawOnce());
		}

		return results;
	}

	/**
	 * 單次抽獎
	 */
	private synchronized String drawOnce() {
		double availableTotalProb = prizes.stream().filter(p -> p.quantity > 0).mapToDouble(p -> p.probability).sum();

		double missProb = 100 - availableTotalProb;

		List<Prize> currentPrizes = new ArrayList<>();
		for (Prize p : prizes) {
			if (p.quantity > 0) {
				currentPrizes.add(p);
			}
		}

		double rand = secureRandom.nextDouble() * 100;
		double cumulative = 0;

		for (Prize p : currentPrizes) {
			cumulative += p.probability;
			if (rand <= cumulative) {
				p.quantity--;
				return "恭喜中獎：" + p.name;
			}
		}

		return "銘謝惠顧";
	}

}
