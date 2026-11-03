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
- Returns a customer based on the turn and the schedule created
- For our case, we also added Seeded randomisation of the customers, ensuring that 1 VIP and 1 Critic will appear in each game, at different turns each run




# What was Modified (Stage 2)

## 1.MenuItem Class
- Created a getPrepUnits() Getter to return the prepunits needed for the respective MenuItems
- Used for Critics to choose the MenuItem with the longest PrepUnit in the CriticCustomer class