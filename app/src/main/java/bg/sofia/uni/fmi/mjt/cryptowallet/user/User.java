package bg.sofia.uni.fmi.mjt.cryptowallet.user;

import com.google.gson.annotations.SerializedName;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class User {
    private static final double INITIAL_MONEY = 0.0;
    private final String username;
    @SerializedName("password")
    private final String hashedPassword;
    private double money;
    private final Set<BoughtCryptocurrency> boughtCryptocurrencies;
    private final Set<SoldCryptocurrency> soldCryptocurrencies;

    public User(String username, String hashedPassword) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.money = INITIAL_MONEY;
        boughtCryptocurrencies = new HashSet<>();
        soldCryptocurrencies = new HashSet<>();
    }

    public String username() {
        return username;
    }

    public String hashedPassword() {
        return hashedPassword;
    }

    public double money() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public Set<BoughtCryptocurrency> boughtCryptocurrencies() {
        return boughtCryptocurrencies;
    }

    public Set<SoldCryptocurrency> soldCryptocurrencies() {
        return soldCryptocurrencies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username) && Objects.equals(hashedPassword, user.hashedPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, hashedPassword);
    }
}
