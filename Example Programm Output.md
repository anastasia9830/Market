```shell
//I used different structure 
Welcome to the Vegetable-Marketplace, what do you want to do?
(1) List all veggies
(2) Search for veggies
(3) Manage veggies (+Authenticate)
(4) Do a transaction (buy/sell)
(5) Price history
(6) Exit

3

Submenu to manage veggies:
(1) Create new veggie (+ authorize)
(2) Manage your veggies (+ authorize)
(3) Return

1

username: AdminUser
Password: supersecret

New veggie category: Carrot
Name: Bio-Möhre
Initial stock amount: 200
Initial price: 1.80

Product created:
Vegetable[id=902, category="Carrot", name="Bio-Möhre", owners="(AdminUser,[1.80,200])", price=1.80]

Welcome to the Vegetable-Marketplace, what do you want to do?
(1) List all veggies
(2) Search for veggies
(3) Manage veggies (+Authenticate)
(4) Do a transaction (buy/sell)
(5) Price history
(6) Exit

4

(1) Buy product
(2) Sell product
(3) Return

1

Search veggie to buy: Drillinge

Available:
Vegetable[id=444, category="Potato", name="Drillinge", owners="(Carl,[22.50,40]),(User2l,[21.80,60])", price=22.15]

How many units to buy? 30
Suppliers:
(1) Carl
(2) User2l

2

Total price: 664.50 EUR. Confirm? (yes/no): yes

Purchase successful.

New stock amount for User2l: 30
New stock amount for Carl: 40

Welcome to the Vegetable-Marketplace, what do you want to do?
(1) List all veggies
(2) Search for veggies
(3) Manage veggies (+Authenticate)
(4) Do a transaction (buy/sell)
(5) Price history
(6) Exit

2

Input search term: Potato

Here are all results:
Vegetable[id=444, category="Potato", name="Drillinge", owners="(User2l,[24.40,30]),(Carl,[24.40,40])", price=24.40]

Welcome to the Vegetable-Marketplace, what do you want to do?
(1) List all veggies
(2) Search for veggies
(3) Manage veggies (+Authenticate)
(4) Do a transaction (buy/sell)
(5) Price history
(6) Exit

5

Get price history of:  444

Price history (for all suppliers):
[10.00, 11.87, 14.98, 17.99, 19.05, 21.80, 22.15, 24.40]

Welcome to the Vegetable-Marketplace, what do you want to do?
(1) List all veggies
(2) Search for veggies
(3) Manage veggies (+Authenticate)
(4) Do a transaction (buy/sell)
(5) Price history
(6) Exit

3

Submenu to manage veggies:
(1) Create new veggie (+ authorize)
(2) Manage your veggies (+ authorize)
(3) Return

2

Username: User2l
Password: secret_password

Your products:
Vegetable[id=444, category="Potato", name="Drillinge", owners="(User2l,[24.40,30]),(Carl,[24.40,40])", price=24.40]

Options:
(1) Change amount
(2) Change price

2

New price= 25.50

Price updated:
Vegetable[id=444, category="Potato", name="Drillinge", owners="(User2l,[25.50,30]),(Carl,[24.40,40])", price=24.40]

Submenu to manage veggies:
(1) Create new veggie (+ authorize)
(2) Manage your veggies (+ authorize)
(3) Return

3

Welcome to the Vegetable-Marketplace, what do you want to do?
(1) List all veggies
(2) Search for veggies
(3) Manage veggies (+Authenticate)
(4) Do a transaction (buy/sell)
(5) Price history
(6) Exit

6
```
