User: check **D: Staff and Interaction Lead

(Bhambri Advika)

Waiter, Chef, and Cashier; CLI menus, action selection, and input validation; Happy Hour activation option.c

**
of the code of stage 2 and give a review of any errors or missing parts

Copilot: I’ll inspect the Stage 2 implementation and trace the staff roles, CLI flow, validation, and Happy Hour behavior against the requested responsibilities.

Find files matching `**/*`

Search for `Happy|happy|Waiter|Chef|Cashier|CLI|menu|Menu|input|validation|stage 2|Stage 2`

Ran terminal command: git status --short; git --no-pager log -5 --oneline

The repository has a dedicated Stage 2 verification suite, so I’m comparing the implementation with both the acceptance notes and executable tests rather than relying only on the current logs.

Read [](), lines 1 to 220
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\staff\\StaffTeam.java",
  "view_range": [
    1,
    220
  ]
}

Read [](), lines 1 to 360
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\test\\integration\\Stage2IntegrationTest.java",
  "view_range": [
    1,
    360
  ]
}

Read [](), lines 1 to 280
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\test\\unit\\OrderMenuTest.java",
  "view_range": [
    1,
    280
  ]
}

Read [](), lines 1 to 320
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\cli\\RestaurantCLI.java",
  "view_range": [
    1,
    320
  ]
}

Read [](), lines 1 to 260
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\staff\\Waiter.java",
  "view_range": [
    1,
    260
  ]
}

Read [](), lines 1 to 260
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\staff\\Cashier.java",
  "view_range": [
    1,
    260
  ]
}

Read [](), lines 1 to 260
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\docs\\Stage2\\Stage_2_Acceptance_Checklist_ZH.md",
  "view_range": [
    1,
    260
  ]
}

Read [](), lines 1 to 360
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\domain\\Restaurant.java",
  "view_range": [
    1,
    360
  ]
}

Read [](), lines 1 to 260
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\staff\\Chef.java",
  "view_range": [
    1,
    260
  ]
}

Read [](), lines 1 to 360
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\control\\GameController.java",
  "view_range": [
    1,
    360
  ]
}

Read [](), lines 150 to 360
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\domain\\Restaurant.java",
  "view_range": [
    150,
    360
  ]
}

Read [](), lines 1 to 320
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\view\\GameView.java",
  "view_range": [
    1,
    320
  ]
}

Read [](), lines 1 to 180
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\contracts\\WaiterAction.java",
  "view_range": [
    1,
    180
  ]
}

Read [](), lines 1 to 220
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\Main.java",
  "view_range": [
    1,
    220
  ]
}

Read [](), lines 170 to 280
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\cli\\RestaurantCLI.java",
  "view_range": [
    170,
    280
  ]
}

Read [](), lines 1 to 220
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\config\\GameConfig.java",
  "view_range": [
    1,
    220
  ]
}

Read [](), lines 1 to 180
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\contracts\\ChefAction.java",
  "view_range": [
    1,
    180
  ]
}

Search for `TODO|FIXME|UnsupportedOperation|catch \(|parseInt|nextInt|readInt`

Search for `happy|Happy|hour|Hour|activate|Activate|activation`

One scope mismatch is already visible: the repository explicitly lists Happy Hour under Stage 3, not Stage 2. I’m now checking whether Stage 2 still has any concrete staff/CLI defects, especially direct API allowance enforcement and malformed input paths.

Read [](), lines 1 to 80
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\README.md",
  "view_range": [
    1,
    80
  ]
}

Read [](), lines 1 to 240
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\scripts\\test-stage2.sh",
  "view_range": [
    1,
    240
  ]
}

Read [](), lines 1 to 260
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\orders\\Order.java",
  "view_range": [
    1,
    260
  ]
}

Read [](), lines 1 to 200
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\scripts\\build.sh",
  "view_range": [
    1,
    200
  ]
}

Read [](), lines 1 to 300
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\entities\\Customer.java",
  "view_range": [
    1,
    300
  ]
}

Search for `already acted|processPayment|executeWaiter|executeChef|lastActionTurn|invalid...`

Ran terminal command: bash scripts/test-stage2.sh

Search for `getWaiterOptions|getChefOptions|submitWaiterAction|submitChefAction|finishTurn`

Search for `class .*Test|new Cashier|new Waiter|new Chef|lastActionTurn`

Ran terminal command: $classes = Join-Path $env:TEMP 'restaurant-stage2-review-classes'; if (Test-Path $classes) { Remove-Item -Recurse -Force $classes }; New-Item -ItemType Directory -Path $classes | Out-Null; $sources = Get-ChildItem -Path src,test -Filter *.java -Recurse | ForEach-Object { $_.FullName }; javac --release 17 -Xlint:all -Werror -d $classes $sources; if ($LASTEXITCODE -eq 0) { java -ea -cp $classes unit.BillCalculatorTest; java -ea -cp $classes unit.OrderMenuTest; java -ea -cp $classes integration.Stage2IntegrationTest (Join-Path (Get-Location) 'test\replays') (Join-Path (Get-Location) 'build\stage2-review') }

Read [](), lines 1 to 120
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\contracts\\ActionResult.java",
  "view_range": [
    1,
    120
  ]
}

Read [](), lines 130 to 175
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\test\\integration\\Stage1SmokeTest.java",
  "view_range": [
    130,
    175
  ]
}

Read [](), lines 1 to 120
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\contracts\\StaffGateway.java",
  "view_range": [
    1,
    120
  ]
}

Search for `Cashier|cashier|Waiter|Chef|CLI|Happy Hour`

Read [](), lines 90 to 112
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\docs\\Stage2\\Integration_Notes.md",
  "view_range": [
    90,
    112
  ]
}

The full Windows compile and Stage 2 suite pass (506 assertions), so the review is now focused on gaps not covered by those tests. I found one concrete staff-boundary issue and one scope-level missing feature; I’m pinning exact lines and checking whether the documentation already disclaims that feature.

Search for `Stage 3|Happy Hour|Not implemented|No Host`

Search for `public ActionResult processPayment|if \(lastActionTurn == turn\)|if \(result....`

Search for `while \(view.phase|chooseWaiterAction|chooseChefAction|System.exit|readInt|In...`

Search for `public ActionResult submitWaiterAction|public ActionResult submitChefAction|i...`

Search for `No Host|Happy Hour implementation|Happy Hour`

## Stage 2 review — D responsibilities

### Findings

| Severity | Area | Finding |
|---|---|---|
| **High, if required for Stage 2** | Happy Hour | No Happy Hour model, activation action, CLI option, controller handling, or tests exists. The repository explicitly defers it to Stage 3 in [README.md](<C:/Users/Payal Bhambri/SC2002_Group_Assignment/README.md:23>) and [Integration_Notes.md](<C:/Users/Payal Bhambri/SC2002_Group_Assignment/docs/Stage2/Integration_Notes.md:103>). If the Stage 2 brief requires it, this is a missing feature. |
| **Low** | Cashier allowance | [Cashier.processPayment](<C:/Users/Payal Bhambri/SC2002_Group_Assignment/src/staff/Cashier.java:11>) does not record the turn when no customer is ready. Therefore, calling it twice in the same turn returns success twice, although the comment says payment collection is “at most once per turn.” The controller calls it only once, so normal CLI behavior is unaffected. |
| **Low** | Test coverage | Existing tests verify duplicate prevention after a successful payment, but not duplicate calls when no payment is available. Add this case if the Cashier API is intended to enforce the allowance independently. |

### Verified as working

- Waiter, Chef, and Cashier classes are present and wired through [StaffTeam.java](<C:/Users/Payal Bhambri/SC2002_Group_Assignment/src/staff/StaffTeam.java>).
- Waiter actions include seating, ordering, serving, and waiting.
- Chef actions include preparation and waiting.
- CLI action selection and retry logic are implemented in [RestaurantCLI.java](<C:/Users/Payal Bhambri/SC2002_Group_Assignment/src/cli/RestaurantCLI.java:57>).
- Invalid, blank, non-numeric, overflow, negative, out-of-range, and BOM-prefixed input are handled.
- Controller rejects null/invalid actions in [GameController.java](<C:/Users/Payal Bhambri/SC2002_Group_Assignment/src/control/GameController.java:63>).
- Stage 2 compilation and integration tests passed: **506 assertions**.

### Overall

The Staff/Interaction Lead implementation is functionally complete for the repository’s declared Stage 2 scope. The only concrete implementation defect is the Cashier’s repeated no-payment call behavior. Happy Hour is absent, but currently documented as a Stage 3 feature rather than a Stage 2 omission.