# Cryptocurrency Wallet Manager 💰💶

This is a project made by me for Modern Java Technologies course in Faculty Of Matematics and Informatics at Sofia University St. Kliment Ohridski

### Description

- This is a `client-server` application, which simulates a virtual cryptocurrency wallet.
- The server must respond to multiple requests, from many clients, at the same time.
- Information about the cryptocurrencies, which is necessary for the server, is available from a public `REST API` - [CoinAPI](https://www.coinapi.io/)
- To use the `REST API` you will have to take your personal `API key` for authentication - [APIkey](https://www.coinapi.io/pricing?apikey) 🔑
- After getting the information from the API, it is cached for the next 30 minutes, so within this time no requests to API are required. If the current information is older than 30 minutes, it is considered invalid and is updated.  
- The server saves user's and crypto information into `JSON` format on the file system, so every time after rebooting, the information is loaded.
- User's sensitive information, such as password, is hashed for security reasons. 

### Supported commands

```
1) help - Show instructions.
2) register <username> <password> - Create an account.
3) login <username> <password> - Log into your profile.
4) list_offerings - Shows information about the currently available cryptocurrencies.
5) buy <offering_code> <amount_money> - Buy cryptocurrency, where <offering_code> is the code of wanted cryptocurrency
   and <amount_money> are invested money.

6) sell <offering_code> - Sell cryptocurrency, where <offering_code> is the code of the crypto you want to sell.
7) get_wallet_summary - Gives you information about your profile (such as available money and currently active investments).
8) get_wallet_overall_summary - Provides the full information about the profit/loss of your investments.
The application compares the price for each cryptocurrency from the time of purchase and its current price to get complete information.

9) disconnect - Save current session and exit.
```

### Errors
- When the application is not used properly, more information about the errors and exceptions (such as messages and stacktraces), is stored into log directory.
- All user errors are stored in directories with the corresponding username, within files named with the time of occurrence.
- All server errors are stored within files into `log/server` directory.
