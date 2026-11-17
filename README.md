# EATZ

EATZ is a Java terminal pager for food trucks. The cook manages a waiting line and the app prints simulated customer text messages to the terminal. Orders and their history are stored in memory for the current session.

## Run locally

Use JDK 17 or later with `java` and `javac` on your PATH. From the project root in PowerShell, run one of the following commands.

| Command | What it does |
|---|---|
| `.\build.ps1 run` | Compiles and starts the interactive console app. |
| `.\build.ps1 driver` | Compiles and runs a model-only demo of adding, cancelling, marking ready, remaking, and rejecting invalid requests. |
| `.\build.ps1 test` | Compiles and runs the JUnit 4 suite using `JUnitCore`. |
| `.\build.ps1` | Compiles the app into `out/main`. |

The project uses `javac` directly and bundles the JUnit dependency in `lib/junit-platform-console-standalone-1.10.2.jar`.

## Use the console

| Choice | Behavior |
|---|---|
| **1. Add order** | Enter a name, phone number, and items. The order joins the back of the line. The console prints its ID and a simulated text with its position and estimated wait. |
| **2. Mark ready** | Removes the front order from the waiting line and prints a simulated ready text. |
| **3. Cancel order** | Removes a waiting order by ID and prints a simulated cancellation text. |
| **4. View line** | Lists waiting orders from front to back with their positions, IDs, customer details, items, and estimated waits. |
| **5. Remake order** | Returns a ready order to the front of the line using its original ID. |
| **6. Skip front order** | Moves the front order to the back and prints a simulated text with its updated position and wait. Each order can be skipped twice. A third attempt leaves the line as it was. |
| **7. Show food menu** | Lists the items accepted when adding an order. |

The food menu includes taco, burrito, quesadilla, churro, and horchata. Enter items on one line, separated by commas, as in `2 taco, churro`. A missing quantity defaults to 1. Repeated entries are combined, with limits of 10 units per item and 20 units per order.

Enter a ten-digit phone number, optionally formatted as `(555) 555-0142`. New orders are limited to three waiting orders per phone number. EATZ asks for confirmation when the name, phone, and items match the most recently added order.

Wait estimates are three minutes times the order's current position. Keep the order ID to cancel or remake the order. The ID is separate from its position, which changes as the line moves.

To quit, send end of input with Ctrl+Z then Enter on Windows or Ctrl+D on macOS and Linux. You can also press Ctrl+C.

## Design and layout

| Path | Responsibility |
|---|---|
| `src/main/java/eatz/model/` | Manages orders, validation, and queue rules independently of terminal input and output. |
| `src/main/java/eatz/view/ConsoleView.java` | Reads raw input and displays prompts, order snapshots, and simulated texts. |
| `src/main/java/eatz/controller/` | `EatzApp` wires the app together. `EatzController` handles menu choices, calls the model, and passes results and errors to the view. |
| `src/main/java/eatz/ModelDriver.java` | Runs a standalone demonstration of the model. |
| `src/test/java/eatz/` | Contains model unit tests and scripted console tests. |

`OrderLine` owns the doubly linked `LinkedDeque<Order>`. It tracks waiting orders by node so it can unlink a cancelled order in constant time once it finds the node. A second map retains orders by ID so a ready order can be remade. The view receives read-only `OrderInfo` snapshots.

## Reflection

### How the design absorbed Skip

D3 predicted four production classes would change for Skip. The implementation touched all four.

| Class | What changed |
|---|---|
| `Order` | Stores a skip count, exposes it through `getSkipCount()`, and provides package-private `addSkip()`. |
| `OrderLine` | Adds `skipFront()`, checks the empty-line and two-skip rules, and replaces the moved order's node in the ID lookup so cancellation still works. |
| `EatzController` | Accepts choice 6, calls `skipFront()`, and sends the updated snapshot or the model's error to the view. |
| `ConsoleView` | Displays choice 6 and prints the order ID, new position, and wait after a successful skip. |

D3 predicted dequeuing before checking the count. The implementation uses `peek()` first, so a rejected third skip leaves both the queue and the lookup untouched. The existing deque operations were sufficient, and Skip required only the planned signature additions.

### Model and view boundary

The view gained a menu entry and a message. It reads the ID, position, and wait from the existing `OrderInfo` snapshot. The skip count and limit stay in the model, and the controller translates model errors into console feedback. The model still runs independently through its tests and `ModelDriver`.

### Changes from the D3 plan

Skip remains the change feature. The app also prints IDs on add and remake so the cook can reuse them, and choice 7 lists the food menu. D3 expected choice 7 to be invalid. The invalid-choice test now uses 8, and the scripted tests check that choice 7 reaches the menu.

`OrderLineTest` covers reordering, updated waits, a single waiting order, empty-line rejection, the third-skip limit, and cancellation and remake after a skip. `OrderTest` covers skip-count initialization, incrementing, and status changes. Existing deque tests cover front and back insertion, removal, empty-deque errors, and null arguments. Parameterless queries such as `size()` and `isEmpty()` are tested in empty and populated states.

### What still needs work

All seven menu choices are reachable. The automated checks cover invalid choices, malformed phone numbers and IDs, unknown menu items, empty-line actions, and the skip limit. The complete D3 scope checklist and Ctrl+C still need a manual terminal pass. Orders are session-only, customer texts are simulated, and wait estimates update when queue positions change.

## Test status

`.\build.ps1 test` ran 75 JUnit 4 tests successfully. The core behavior is covered by model tests. The separate controller tests check that terminal commands reach the model and that the view displays the result.

## Next Steps

### Final build

| Order | Work remaining | Proposed owner |
|---|---|---|
| 1 | Walk the D3 bad-input checklist in a real terminal, including Ctrl+C and end of input. | Bagu leads, whole team participates |
| 2 | Fix any queue or validation defects found in the walkthrough and cover them with model tests. | Hannah owns queue behavior, Rohit owns validation |
| 3 | Rehearse the demo and fix any unclear prompts or line formatting it exposes. | Bagu owns console output, Ian owns the demo run |
| 4 | Run the full suite on the final build and coordinate the merge. | Ian |

**Stretch goals.** Keep Skip as the implemented stretch feature. Drop the live countdown for this build. Position-based estimates already cover the demo, and a countdown would add timer and display updates beyond the current design.

### Three-minute demo

| Time | Walkthrough |
|---|---|
| 0:00 to 0:55 | Show the food menu with 7. Add Ana and Ben, point out the order IDs and simulated texts, and view the line with 4. |
| 0:55 to 1:45 | Choose 6 to skip Ana. Show the simulated text with her new position and wait, then view the line to confirm Ben is first. |
| 1:45 to 2:40 | Mark Ben ready with 2, remake his order with 5 using ID 2, then cancel Ana's order with 3 using ID 1. |
| 2:40 to 3:00 | Enter 8 to show that bad input returns to the menu. End input to exit. |

## Attribution

The team used GenAI mainly to verify that the code worked, including checking behavior and running tests. The GenAI-assisted label refers to that project-wide verification support. The GenAI note below also records help with implementation and documentation.

The author and validator columns come from the Section 8 plan. The review and test columns list proposed assignments for the team to confirm.

| File | Who worked on it | Validator | Reviewed (proposed) | Tested (proposed) | GenAI assistance |
|---|---|---|---|---|---|
| `model/LinkedDeque.java` (includes `Node`) | Hannah | Ian | Bagu | Rohit | GenAI-assisted |
| `model/OrderLine.java` | Hannah | Bagu | Rohit | Ian | GenAI-assisted |
| `model/Order.java` | Rohit | Hannah | Bagu | Ian | GenAI-assisted |
| `model/OrderStatus.java` | Rohit | Ian | Hannah | Bagu | GenAI-assisted |
| `model/OrderInfo.java` | Rohit | Bagu | Hannah | Ian | GenAI-assisted |
| `model/OrderValidator.java` | Rohit | Ian | Hannah | Bagu | GenAI-assisted |
| `view/ConsoleView.java` | Bagu | Hannah | Ian | Rohit | GenAI-assisted |
| `controller/EatzController.java` | Ian | Hannah | Rohit | Bagu | GenAI-assisted |
| `controller/EatzApp.java` | Ian | Rohit | Bagu | Hannah | GenAI-assisted |
| `ModelDriver.java` | Bagu | Hannah | Rohit | Ian | GenAI-assisted |
| `build.ps1`, `.gitignore` | Bagu | Ian | Hannah | Rohit | GenAI-assisted |
| `test/model/LinkedDequeTest.java` | Hannah | Rohit | Ian | Bagu | GenAI-assisted |
| `test/model/OrderLineTest.java` | Hannah | Ian | Bagu | Rohit | GenAI-assisted |
| `test/model/OrderTest.java` | Bagu | Rohit | Ian | Hannah | GenAI-assisted |
| `test/model/OrderInfoTest.java` | Rohit | Hannah | Bagu | Ian | GenAI-assisted |
| `test/model/OrderValidatorTest.java` | Rohit | Ian | Hannah | Bagu | GenAI-assisted |
| `test/controller/EatzControllerTest.java` | Ian | Bagu | Hannah | Rohit | GenAI-assisted |

### GenAI note

***During the preparation of this assignment, we used ChatGPT, in the development of EATZ for help and guidance in creating the files for the program when we were completely stuck. After using this tool, I reviewed and edited the content as needed to ensure its accuracy and take full responsibility for the content in relation to grading.***

Codex also helped audit the checkpoint against the rubric, correct menu routing, add JUnit coverage, and update this README.
