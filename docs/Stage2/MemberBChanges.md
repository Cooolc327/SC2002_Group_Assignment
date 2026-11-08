# What was added (Stage 2)

## 1.Vip Customers
- New Type of Customer
- Loses 5 Satisfaction per Waiting Turn
- 10% Discount off on their total bill

## 2.Critic Customer
- New Type of Customer
- Loses 12 Satisfaction per Waiting Turn
- No Discount Given
- Distinct Order Preference or reaction to service, for us we decided to have critics always choose the MenuItem with the longest PrepUnits

## 3.CustomerFactory
- Decides what type of customer each arrival will be, removing the need for the turnController to decide
- New types of customers does not have to be edited into the controller itself
- Returns a fresh customer based on its arrival-order ID (1-6) and the seeded schedule
- For our case, we also added Seeded randomisation of the customers, ensuring that 1 VIP and 1 Critic appear among six arrivals. The same seed reproduces the same order on every run; different seeds may change their positions




# What was Modified (Stage 2)

## 1.MenuItem Class
- Created a getPrepUnits() Getter to return the prepunits needed for the respective MenuItems
- Used for Critics to choose the MenuItem with the longest PrepUnit in the CriticCustomer class

## Integration interface

Critic overrides the existing `chooseItem(Menu)` method. The controller supplies
customer IDs 1-6 to `createCustomer(id)`; actual arrival turns remain in GameConfig.
See [integration notes](Integration_Notes.md) for the implemented corrections and checks.
