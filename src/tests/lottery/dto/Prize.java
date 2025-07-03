package tests.lottery.dto;

public class Prize {
	public String name; // 獎品名稱
	public int quantity; // 剩餘數量
	public double probability; // 原始機率（%）

	public Prize(String name, int quantity, double probability) {
		this.name = name;
		this.quantity = quantity;
		this.probability = probability;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public double getProbability() {
		return probability;
	}

	public void setProbability(double probability) {
		this.probability = probability;
	}

}
