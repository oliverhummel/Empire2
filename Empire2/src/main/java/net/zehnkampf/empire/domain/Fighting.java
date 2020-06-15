package net.zehnkampf.empire.domain;

public interface Fighting {
	public int getAttackStrength();
	public int getDefenseStrength();
	
	public int getHealth();
	public void decreaseHealth(int health);
	public void setHealth(int health);
	
	public Player getOwner();
}
