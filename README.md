# Modern-DevOps-Practices-Project

This is a project made by me for Modern DevOps Practices course in 
Faculty Of Matematics and Informatics at Sofia University St. Kliment Ohridski.

In this project i use some of the best practices widely spread nowadays, 
that we have learned during the semester, to build a complete automated 
software delivery process using pipelines.

## Application - Cryptocurrency Wallet Manager 💰💶

- This is a `client-server` application,
  which simulates a virtual cryptocurrency wallet.
- The server must respond to multiple requests,
  from many clients, at the same time.
- Information about the cryptocurrencies,
  which is necessary for the server, is available
  from a public `REST API` - [CoinAPI](https://www.coinapi.io/)
- To use the `REST API` you will have to take your personal
  `API key` for authentication - [APIkey](https://www.coinapi.io/pricing?apikey) 🔑
- After getting the information from the API,
  it is cached for the next 30 minutes, so within this time
  no requests to API are required. If the current information
  is older than 30 minutes, it is considered invalid and is updated.  
- The server saves user's and crypto information into `JSON` format
  on the file system, so every time after rebooting, the information is loaded.
- User's sensitive information, such as password,
  is hashed for security reasons. 

### Supported commands

- help - Show instructions.
- register <username> <password> - Create an account.
- login <username> <password> - Log into your profile.
- list_offerings - Shows information about the currently
   available cryptocurrencies.
- buy <offering_code> <amount_money> - Buy cryptocurrency,
   where <offering_code> is the code of wanted cryptocurrency
   and <amount_money> are invested money.

- sell <offering_code> - Sell cryptocurrency, where
  <offering_code> is the code of the crypto you want to sell.
- get_wallet_summary - Gives you information about your profile
  (such as available money and currently active investments).
- get_wallet_overall_summary - Provides the full information
  about the profit/loss of your investments. The application
  compares the price for each cryptocurrency from the time of
  purchase and its current price to get complete information.

- disconnect - Save current session and exit.
