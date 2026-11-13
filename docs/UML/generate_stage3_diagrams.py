#!/usr/bin/env python3
"""Optional Stage 3 documentation generator. Requires reportlab, not used by Java.
Reuses the Stage 2 drawing primitives so PDF/SVG/drawio share one vector model.
"""
from __future__ import annotations
import argparse, hashlib, html, inspect, json, math, subprocess, textwrap
from pathlib import Path
import generate_stage2_diagrams as u
from reportlab.pdfbase.pdfmetrics import stringWidth

BOXES = u.BOXES
EDGES = list(u.EDGES)
Router = u.Router
note = u.note
INK, MUTED, BLUE, WHITE, LINE = u.INK, u.MUTED, u.BLUE, u.WHITE, u.LINE
IMPLEMENTATION = '3f5e682f775734a6a305fc159abd89aaf76257c3'
STAGE2 = 'ad6f39ba20102ef3fa20778cb19e70ab3cb69c21'

def register_fonts():
    """Embed locally available metric-compatible fonts for portable PDF rendering."""
    from reportlab.pdfbase import pdfmetrics
    from reportlab.pdfbase.ttfonts import TTFont
    import reportlab
    system=Path('/System/Library/Fonts/Supplemental')
    vera=Path(reportlab.__file__).parent/'fonts'
    choices={
      'Helvetica': [system/'Arial.ttf',vera/'Vera.ttf'],
      'Helvetica-Bold': [system/'Arial Bold.ttf',vera/'VeraBd.ttf'],
      'Helvetica-Oblique': [system/'Arial Italic.ttf',vera/'VeraIt.ttf'],
      'Helvetica-BoldOblique': [system/'Arial Bold Italic.ttf',vera/'VeraBI.ttf'],
      'Courier': [system/'Courier New.ttf',vera/'VeraMono.ttf'],
    }
    for name,candidates in choices.items():
        for font in candidates:
            if font.is_file():
                pdfmetrics.registerFont(TTFont(name,str(font)));break
    pdfmetrics.registerFontFamily('Helvetica',normal='Helvetica',bold='Helvetica-Bold',italic='Helvetica-Oblique',boldItalic='Helvetica-BoldOblique')

register_fonts()

class Drawing(u.Drawing):
    def __init__(self, path, width, height, title):
        super().__init__(path, width, height, title)
        self.c.setAuthor('SC2002 Restaurant Rush group - Stage 3 design evidence')
        self.mx.find('diagram').set('name', 'Stage 3')

def replace_box(id, attrs=None, ops=None):
    b=BOXES[id]
    BOXES[id]=u.Box(b.id,b.name,b.stereo,b.x,b.y,b.w,b.attrs if attrs is None else attrs,
                    b.ops if ops is None else ops,True,b.abstract)

def model():
    u.model()
    for b in BOXES.values():
        b.changed=False;b.x+=50;b.y=int(b.y*1.25+40)
    replace_box('Main')
    replace_box('CLI',ops=BOXES['CLI'].ops+['- chooseTurnStart(view: GameView): void','- chooseHostAction(view: GameView): void'])
    replace_box('Controller',ops=BOXES['Controller'].ops+['+ submitTurnStartDecision(activateHappyHour: boolean): ActionResult','+ submitHostAction(action: HostAction): ActionResult'])
    replace_box('Phase',attrs=['NOT_STARTED','AWAIT_TURN_START','AWAIT_HOST','AWAIT_WAITER','AWAIT_CHEF','AUTO','TURN_ENDED','FINISHED'])
    replace_box('Restaurant',attrs=BOXES['Restaurant'].attrs+['- stage3Enabled: boolean {readOnly}','- lastTurnStartPolicyTurn: int'],
      ops=BOXES['Restaurant'].ops+['+ Restaurant(orderFactory: OrderPort.Factory, stage3Enabled: boolean)','+ resolveTurnStart(activateHappyHour: boolean): ActionResult','+ getHostOptions(): List<HostAction>','+ getHappyHourRemainingTurns(): int','+ isActionTurnOpen(turn: int): boolean'])
    for id in ['Gateway','Team']:
        replace_box(id,ops=['+ executeHost(action: HostAction, restaurant: Restaurant, turn: int): ActionResult']+BOXES[id].ops)
    replace_box('Customer',ops=BOXES['Customer'].ops+['+ recoverWaitingSatisfaction(points: int): void'])
    replace_box('Critic')
    replace_box('Menu',attrs=['- combosEnabled: boolean {readOnly}'],ops=['+ Menu()','+ Menu(combosEnabled: boolean)','+ getItems(): List<MenuItem>','+ getCombos(): List<ComboMeal>','+ getOrderableItems(): List<MenuItem>','+ contains(item: MenuItem): boolean'])
    replace_box('Bills',attrs=['~+ HAPPY_HOUR_DISCOUNT_PERCENT: int = 20 {readOnly}'],ops=['- BillCalculator()','~+ billFor(customer: Customer, item: MenuItem): int','~+ billFor(customer: Customer, item: MenuItem, happyHourActive: boolean): int'])
    replace_box('View',attrs=BOXES['View'].attrs+['- stage3Enabled: boolean'])
    u.add('HH','HappyHourPolicy','<<pricing policy>>',2570,250,390,['~+ DURATION_TURNS: int = 2 {readOnly}','~+ SATISFACTION_RECOVERY: int = 15 {readOnly}','- activationTurn: Integer'],['+ hasBeenUsed(): boolean','+ activate(turn: int): void','+ remainingTurns(turn: int): int','+ isActive(turn: int): boolean'],True)
    u.add('Discount','Discount','<<utility>>',2570,590,390,['~- PERCENT_BASE: int = 100 {readOnly}'],['- Discount()','~+ apply(cents: int, percent: int): int'],True)
    u.add('HHView','GameView.HappyHourView','<<immutable record>>',2570,930,390,['- canActivate: boolean','- used: boolean','- remainingTurns: int'],[],True)
    u.add('StaffView','GameView.StaffView','<<immutable record>>',2570,1200,450,['- hostAvailable: boolean','- hostOptions: List<HostAction>','- waiterAvailable: boolean','- chefAvailable: boolean','- cashierAvailable: boolean','- waiterOptions: List<WaiterAction>','- chefOptions: List<ChefAction>'],['+ StaffView(...) {List.copyOf}'],True)
    u.add('Combo','ComboMeal','<<MenuItem extension>>',2570,1870,390,['~+ COMBO_DISCOUNT_PERCENT: int = 10 {readOnly}'],['+ ComboMeal(first: MenuItem, second: MenuItem)','+ getComponents(): List<MenuItem>'],True)
    u.add('Host','Host','<<staff entity>>',80,2520,350,['- lastActionTurn: int = 0'],['+ execute(action: HostAction, restaurant: Restaurant, turn: int): ActionResult'],True)
    u.add('HA','HostAction','<<record>>',570,2520,390,['- type: Type','- customerId: int','- tableId: int'],['~+ seat(customerId: int, tableId: int): HostAction','~+ waitTurn(): HostAction'],True)
    u.add('HT','HostAction.Type','<<enumeration>>',80,2770,350,['SEAT','WAIT'],[],True)
    EDGES[:]=list(u.EDGES)+[
      ('Restaurant','HH','comp','-happyHour','1','1'),('Team','Host','comp','-host','1','1'),
      ('Host','Restaurant','dep','shared seating validation','',''),('Host','HA','dep','<<use>>','',''),('HA','HT','assoc','-type','','1'),
      ('Menu','Combo','comp','-combos','1','3'),('Combo','Item','inherit','','',''),
      ('Combo','Item','assoc','-components {shared}','0..*','2'),('Combo','Discount','dep','combo rounding','',''),
      ('Bills','Discount','dep','percentage rounding','',''),('View','HHView','assoc','-happyHour','','1'),
      ('View','StaffView','assoc','-staff','','1'),('StaffView','HA','dep','-hostOptions','','')]

Sequence=u.Sequence
PARTICIPANTS=[('M','Manager','<<actor>>'),('CLI','cli:RestaurantCLI','<<boundary>>'),('C','game:GameController','<<control>>'),
 ('Fn','factoryFn:IntFunction','<<method reference>>'),('F','customers:CustomerFactory','<<factory>>'),
 ('S','staff:StaffTeam','<<StaffGateway>>'),('H','host:Host','<<staff>>'),('W','waiter:Waiter','<<staff>>'),
 ('Ch','chef:Chef','<<staff>>'),('Ca','cashier:Cashier','<<staff>>'),('R','restaurant:Restaurant','<<domain>>'),
 ('P','happyHour:HappyHourPolicy','<<policy>>'),('V','vip:VipCustomer','<<created on turn 4>>'),('B','BillCalculator','<<utility>>'),
 ('OFn','orderFn:OrderPort.Factory','<<Order::new>>'),('O','order2:Order','<<created on turn 4>>'),('A','seating:SeatingArea','<<entity>>'),('G','GameResult','<<enumeration>>')]

def sequence_model():
    s=Sequence();m=s.msg
    s.note('Default seed 24369, entering turn 4 of stage3-victory.txt. Regular #1 is READY_TO_PAY from turn 3, at Table 1; Table 2 is free. VIP #2 arrives now. Normal branch activates Happy Hour on turn 4. Alternative keeps it unused with the same service priorities. All 18 turns still run.')
    s.section('TURN 4 - arrival, manager pricing choice and independent Host seating')
    m('CLI','C','startTurn()');m('C','Fn','apply(2) [arrival turn]');m('Fn','F','createCustomer(2)')
    m('F','V','VipCustomer(2)',create=True);m('Fn','C','vip {WAITING, satisfaction=100}',ret=True)
    m('C','R','beginTurn(4)');m('C','R','registerArrival(vip)');m('R','A','addToQueue(vip)')
    m('C','CLI','GameView {AWAIT_TURN_START, canActivate=true}',ret=True)
    s.start('alt','[manager chooses Activate]')
    m('M','CLI','Happy Hour choice = 1');m('CLI','C','submitTurnStartDecision(true)');m('C','R','resolveTurnStart(true)')
    m('R','P','hasBeenUsed() = false; activate(4)');m('R','P','isActive(4) = true')
    s.start('loop','[each registered customer; Customer guards recovery eligibility]')
    m('R','V','recoverWaitingSatisfaction(15) [VIP 100 -> 100]');s.end()
    s.note('Customer #1 is READY_TO_PAY, so receives no recovery. Each turn-start policy is applied only once; invalid or repeated choices leave state and staff capacity unchanged.')
    s.otherwise('[manager keeps Happy Hour unused]')
    m('M','CLI','Happy Hour choice = 0');m('CLI','C','submitTurnStartDecision(false)');m('C','R','resolveTurnStart(false)')
    s.end();m('C','CLI','ActionResult accepted; phase=AWAIT_HOST',ret=True)
    m('M','CLI','Choose Host: seat VIP #2 at Table 2');m('CLI','C','submitHostAction(HostAction.seat(2,2))')
    m('C','S','executeHost(action, r, 4)');m('S','H','execute(action, r, 4)');m('H','R','seatCustomer(2,2)')
    m('R','A','seat(vip,2)');m('A','V','seat() [WAITING -> SEATED]')
    m('C','CLI','accepted; phase=AWAIT_WAITER; fresh Waiter options',ret=True)
    s.section('TURN 4 - order choice, locked VIP/Happy Hour bill, READY and first waiting loss')
    m('M','CLI','Choose Waiter: take VIP order');m('CLI','C','submitWaiterAction(WaiterAction.takeOrder(2))')
    m('C','S','executeWaiter(action,r,4)');m('S','W','execute(action,r,4)');m('W','R','placeOrder(2)')
    m('R','V','chooseItem(menu)');m('V','R','Burger [customer choice, not manager choice]',ret=True)
    m('R','P','isActive(4)');m('R','B','billFor(vip,Burger,happyHourActive)');m('B','V','getBillDiscountPercent() = 10')
    s.start('alt','[Happy Hour active]');s.note('Discount.apply: 1500 -> VIP 1350 -> Happy Hour 1080 cents, rounding each step. New orders only.')
    s.otherwise('[Happy Hour unused]');s.note('Customer discount only: 1500 -> 1350 cents. Existing orders remain unchanged in either branch.');s.end()
    m('B','R','billCents = 1080 [normal] / 1350 [alternative]',ret=True)
    m('R','OFn','create(2,vip,Burger,billCents)');m('OFn','O','Order(2,vip,Burger,billCents)',create=True)
    m('R','V','placeOrder() [SEATED -> ORDERED]');m('C','CLI','accepted; phase=AWAIT_CHEF',ret=True)
    m('M','CLI','Choose Chef: prepare order 2');m('CLI','C','submitChefAction(ChefAction.prepare(2))')
    m('C','S','executeChef(action,r,4)');m('S','Ch','execute(action,r,4)');m('Ch','R','advancePreparation(2,4)')
    m('R','O','advanceOneUnit(4) [1/1 -> READY; readyTurn=4]')
    s.note('Chef may prepare the order taken this turn. The Waiter has already acted; a dish becoming READY on turn 4 cannot be served until a later turn.')
    m('CLI','C','finishTurn()');m('C','R','advanceEating(4)');m('C','S','processPayment(r,4)');m('S','Ca','processPayment(r,4)')
    s.ref('Automatic cashier follows Restaurant.findOldestPayableCustomer / settlePayment: customer #1 pays $9.00 on turn 4; VIP #2 is not ready. The pre-existing Salad bill is unchanged by Happy Hour.')
    m('C','R','applyWaitingPenalty(4)');m('R','V','applyWaitingPenalty() [100 -> 95]');m('C','R','completeTurn(4)')
    m('C','G','evaluate(r.getSummary())');m('G','C','IN_PROGRESS [4 < 18]',ret=True)
    s.section('TURN 5 - one turn remaining, recovery and later-turn serving')
    m('CLI','C','startTurn()');m('C','R','beginTurn(5)');m('CLI','C','submitTurnStartDecision(false) [no reactivation]')
    m('C','R','resolveTurnStart(false)')
    s.start('alt','[Happy Hour activated on turn 4]');m('R','P','isActive(5)=true; remainingTurns(5)=1')
    m('R','V','recoverWaitingSatisfaction(15) [95 -> 100, cap 100]')
    s.otherwise('[Happy Hour remains unused]');s.note('No recovery; VIP satisfaction remains 95.');s.end()
    s.ref('CLI displays updated GameView. Host chooses Wait through Controller -> StaffTeam -> Host; its capacity remains independent of Waiter.')
    m('M','CLI','Choose Waiter: serve order 2');m('CLI','C','submitWaiterAction(WaiterAction.serve(2))')
    m('C','S','executeWaiter(action,r,5)');m('S','W','execute(action,r,5)');m('W','R','serveOrder(2,5)')
    m('R','O','markServed(5) [readyTurn=4 < 5]');m('R','V','serve(5) [ORDERED -> SERVED]')
    s.ref('Chef chooses Wait. finishTurn(5) cannot finish VIP eating in the same turn; no VIP payment or waiting loss occurs after service. completeTurn(5); GameResult remains IN_PROGRESS.')
    s.section('TURNS 6-7 - eating, deferred payment, locked bill and table release')
    s.ref('Turn 6: startTurn; resolveTurnStart(false); Happy Hour remaining is 0. Host/Waiter/Chef choose Wait. These follow the same guarded interfaces.')
    m('CLI','C','finishTurn() [turn 6]');m('C','R','advanceEating(6)');m('R','V','finishedEating(6) [servedTurn=5 < 6]')
    m('V','R','READY_TO_PAY; readyToPayTurn=6',ret=True);m('C','S','processPayment(r,6)');m('S','Ca','processPayment(r,6)')
    m('Ca','R','findOldestPayableCustomer(6)');m('R','Ca','VIP excluded [readyToPayTurn is not < 6]',ret=True)
    s.ref('Turn 6 penalties/completion/result check run normally. Turn 7 registers Regular #3, resolves the opening, seats #3 with Host, takes its Pasta order and prepares the first unit. VIP #2 remains ready to pay.')
    m('CLI','C','finishTurn() [turn 7]');m('C','R','advanceEating(7)');m('C','S','processPayment(r,7)');m('S','Ca','processPayment(r,7)')
    m('Ca','R','findOldestPayableCustomer(7)');m('R','Ca','customerId=2 [readyToPayTurn=6 < 7]',ret=True)
    m('Ca','R','settlePayment(2,7)');m('R','O','markPaid(7,satisfaction) [1080,100 / 1350,95]');m('R','V','pay() [READY_TO_PAY -> PAID]')
    m('R','A','removeCustomer(vip) [Table 2 released]')
    s.note('Revenue and average use the locked paid bill and recorded satisfaction. Happy Hour expiration does not recalculate the order. Normal branch: $10.80, satisfaction 100. Alternative: $13.50, satisfaction 95.')
    s.section('TURNS 8-18 - remaining mixed customers, combos and final result')
    s.start('loop','[each remaining turn, up to turn 18]')
    s.ref('Actual stage3-victory priorities: Host seats first queued customer; Waiter serves, otherwise takes order, otherwise seats; Chef prepares the first eligible order. New combos follow the same Order/Chef/Waiter/Cashier lifecycle.')
    m('CLI','C','finishTurn()');m('C','R','advanceEating(currentTurn)');m('C','S','processPayment(r,currentTurn)');m('C','R','applyWaitingPenalty(currentTurn); completeTurn(currentTurn)')
    m('C','G','evaluate(r.getSummary())')
    s.start('alt','[turnsUsed < 18]');m('G','C','IN_PROGRESS',ret=True)
    s.otherwise('[turnsUsed == 18]');m('G','C','VICTORY [paid>=4, revenue>=4500, average>=60]',ret=True)
    m('C','CLI','GameView {FINISHED, VICTORY}',ret=True);s.end();s.end()
    s.note('Both full runs pay 5 customers. Activate on turn 4: $83.70, average 87.20. Keep unused with identical service priorities: $86.40, average 86.20. The choice changes money and satisfaction, not the victory label in these runs. Critic #6 cannot pay by turn 18.')
    return s


def class_diagram(out):
    model();W=3100;H=3260
    for a in BOXES.values():
        assert a.x>=20 and a.y>=60 and a.x+a.w<W-10 and a.y+a.h<H-60,(a.id,a.h)
        for b in BOXES.values():
            if a.id<b.id:
                assert not(a.x<b.x+b.w+8 and b.x<a.x+a.w+8 and a.y<b.y+b.h+8 and b.y<a.y+a.h+8),('overlap',a.id,b.id,a.h,b.h)
    d=Drawing(out/'Stage3_UML_Diagram',W,H,'Restaurant Rush - Stage 3 Class Diagram')
    d.text(30,30,'Restaurant Rush - Stage 3 Class Diagram',20,'Helvetica-Bold')
    d.text(30,49,'Implementation 3f5e682 | Exact Stage 2 comparison baseline: stage-2 / ad6f39b | Blue header = added or changed in Stage 3',11,color=MUTED)
    extras=[]
    extras.append(note(d,'Legend',2020,300,410,['+ public; - private; # protected. Underlined = static.','Hollow triangle = inheritance; dashed triangle = realization.','Filled diamond = composition; hollow diamond = aggregation.','Dashed open arrow = dependency. Numbers are multiplicities.','Reference-valued fields may be represented by association roles. Only key operations are shown.'],'UML notation'))
    extras.append(note(d,'DTOs',2570,2170,430,['Nested immutable records:','CustomerView','TableView','MenuItemView','OrderView','StaffView and HappyHourView are explicit boxes.','Lists copied with List.copyOf; record accessors are implicit.'],'GameView records'))
    extras.append(note(d,'OO',30,2930,570,['Customer state is private and changed through guarded operations.','Loss, food choice and discount dispatch through Customer methods.','StaffTeam owns independent Host/Waiter/Chef/Cashier. Restaurant owns HappyHourPolicy. Combo components are shared references, not exclusive ownership.'],'OO examples'))
    extras.append(note(d,'FunctionNote',780,2930,2270,['Main passes customers::createCustomer as an IntFunction. CustomerFactory does not implement IntFunction, so there is no realization arrow between them. The controller stores the function, not a concrete CustomerFactory.','Default seed 24369: Regular, VIP, Regular, Regular, Regular, Critic; six arrivals at 1/4/7/10/13/16. Regular/VIP cycle through the six orderable items; Critic chooses the most prep units, first tie. Happy Hour starts once, covers the current and next turn, and restores 15 waiting satisfaction at each opening (cap 100). Bills lock VIP then Happy Hour discounts when taken.'],'Stage 3 dispatch, composition and policy'))
    obstacles=[(b.x,b.y,b.w,b.h) for b in BOXES.values()]+extras
    router=Router(W,H-35,obstacles)
    labels=[];routes=[]
    def clear_label(x,y,text,size=9):
        ww=stringWidth(text,'Helvetica',size)+8
        bb=(x-ww/2,y-size-3,ww,size+7)
        if bb[0]<8 or bb[0]+ww>W-8 or bb[1]<60 or bb[1]+bb[3]>H-35:return False
        for ox,oy,ow,oh in obstacles+[(lx-lw/2-3,ly-13,lw+6,18) for lx,ly,lw in labels]:
            if bb[0]<ox+ow+2 and ox<bb[0]+bb[2]+2 and bb[1]<oy+oh+2 and oy<bb[1]+bb[3]+2:return False
        return True
    def save_label(x,y,text,size=9):
        labels.append((x,y,stringWidth(text,'Helvetica',size)));d.label(x,y,text,size)
    priority={'inherit':0,'realize':1,'comp':2,'agg':3,'assoc':4,'dep':5}
    for a,b,kind,label,sm,tm in sorted(EDGES,key=lambda e:priority[e[2]]):
        pts=router.route(BOXES[a],BOXES[b]);routes.append((a,b,kind,pts))
        d.arrow(pts,kind);d.mx_edge(pts,label,kind,a,b)
        if label:
            segments=sorted([(math.dist(p,q),p,q) for p,q in zip(pts,pts[1:])],reverse=True)
            candidates=[]
            for length,p,q in segments:
                for f in [.5,.25,.75]:
                    xx=p[0]+(q[0]-p[0])*f;yy=p[1]+(q[1]-p[1])*f
                    if p[1]==q[1]:
                        for offset in [-5,16,-20,30]:candidates.append((xx,yy+offset))
                    else:
                        half=stringWidth(label,'Helvetica',9)/2
                        for offset in [half+7,-half-7,half+20,-half-20]:candidates.append((xx+offset,yy))
            for xx,yy in candidates:
                if clear_label(xx,yy,label):save_label(xx,yy,label);break
            else:raise ValueError('No clear role label: '+a+' '+b+' '+label)
        for mult,p,q in [(sm,pts[0],pts[1]),(tm,pts[-1],pts[-2])]:
            if mult:
                dx=q[0]-p[0];dy=q[1]-p[1];length=math.hypot(dx,dy)
                candidates=[(p[0]+dx/length*distance-dy/length*offset,
                             p[1]+dy/length*distance+dx/length*offset)
                            for distance in [18,28,38,48,58,68,78,88] for offset in [10,-10,22,-22,34,-34]]
                for xx,yy in candidates:
                    if clear_label(xx,yy,mult):save_label(xx,yy,mult);break
                else:raise ValueError('No clear multiplicity: '+a+' '+b+' '+mult)
    for b in BOXES.values():b.draw(d)
    d.text(30,H-20,'Source: lichen/stage_3 at 3f5e682. Names, key signatures and multiplicities checked 13 November 2026. Lists/record accessors omitted where implicit.',10,color=MUTED)
    d.finish()
    (out/'stage3-class-layout.json').write_text(json.dumps({'boxes':[{**b.__dict__} for b in BOXES.values()],'routes':routes},indent=2))
    return len(BOXES),len(EDGES)


def sequence_diagram(out):
    s=sequence_model();W=3620;xs={p[0]:80+i*200 for i,p in enumerate(PARTICIPANTS)}
    y=215;rows=[];creation={}
    def wrap(s,width,font='Helvetica',size=11):
        lines=[];line=''
        for word in s.split():
            t=(line+' '+word).strip()
            if line and stringWidth(t,font,size)>width:lines.append(line);line=word
            else:line=t
        if line:lines.append(line)
        return lines
    for ev in s.events:
        kind=ev['kind']
        if kind=='message':
            width=abs(xs[ev['a']]-xs[ev['b']])-24 if ev['a']!=ev['b'] else 260
            lines=wrap(ev['label'],max(140,width))
            h=max(30,len(lines)*14+15)
            if ev['create']:h=max(h,65)
        elif kind in ('note','ref'):
            lines=wrap(ev['label'],W-190,size=11);h=len(lines)*15+34
        elif kind=='section':lines=[ev['label']];h=46
        else:lines=wrap(ev['label'],W-250);h=len(lines)*14+35
        row={**ev,'y':y,'h':h,'lines':lines};rows.append(row)
        if ev.get('create'):creation[ev['b']]=y+h-8
        y+=h
    H=y+75;d=Drawing(out/'Stage3_Happy_Hour_Sequence',W,H,'Restaurant Rush - Stage 3 Happy Hour sequence and manager choice')
    d.text(35,32,'Restaurant Rush - Stage 3: a manager choice changes the locked bill and paid satisfaction',21,'Helvetica-Bold')
    d.text(35,54,'Concrete seed-24369 replay, turns 4-18 | Solid arrow: synchronous call; dashed arrow: return | ref: abbreviated actual subinteraction',11,color=MUTED)
    d.text(35,73,'Implementation 3f5e682. Normal and no-activation alternatives use the same Stage 3 service priorities. Both win; amounts and satisfaction differ.',11,color=MUTED)
    for i,f in enumerate(s.frames):
        y0=rows[f['start']]['y']-6;ye=rows[f['end']-1]['y']+rows[f['end']-1]['h']+1
        inset=15 if f['start']>80 else 0
        # Nested result guard sits inside the remaining-turn loop.
        if f['op']=='alt' and f['label']=='[turnsUsed < 18]':inset=28
        x=25+inset;w=W-50-2*inset
        d.rect(x,y0,w,ye-y0,WHITE,LINE,.8)
        d.rect(x,y0,58,23,'#f0f0f0',LINE,.8);d.text(x+8,y0+16,f['op'],11,'Helvetica-Bold')
        d.mx_node('frame'+str(i),x,y0,w,ye-y0,'<b>'+f['op']+'</b> '+html.escape(f['label']),WHITE,'fillColor=none;verticalAlign=top;')
        for index,label in f['dividers']:
            yy=rows[index]['y'];d.line([(x,yy),(x+w,yy)],LINE,.8,True)
    for id,name,stereo in PARTICIPANTS:
        x=xs[id];top=creation.get(id,155)-23
        if id=='M':
            # Actor symbol, with its own lifeline.
            d.c.setStrokeColor(INK);d.c.setLineWidth(1);d.c.circle(x,H-124,8,stroke=1,fill=0)
            d.svg.append(f'<circle cx="{x}" cy="124" r="8" fill="white" stroke="{INK}"/>')
            d.line([(x,132),(x,154)]);d.line([(x-15,140),(x+15,140)]);d.line([(x,154),(x-13,169)]);d.line([(x,154),(x+13,169)])
            d.text(x,188,name,11,'Helvetica-Bold',align='center');life=200
            d.mx_node('participant'+id,x-45,110,90,85,name,WHITE,'shape=umlActor;align=center;')
        else:
            d.rect(x-88,top,176,46,BLUE if id in ('Fn','F','H','P','V','B') else WHITE,INK,.9)
            d.text(x,top+15,stereo,9,align='center');d.text(x,top+32,name,10,'Helvetica-Bold',align='center')
            d.mx_node('participant'+id,x-88,top,176,46,'<div style="text-align:center;padding:4px">'+html.escape(stereo)+'<br/><b>'+html.escape(name)+'</b></div>',BLUE if id in ('Fn','F','H','P','V','B') else WHITE)
            life=top+46
        d.line([(x,life),(x,H-45)],'#9299a1',.65,True)
        d.mx_edge([(x,life),(x,H-45)],'', 'lifeline')
    n=0
    for i,row in enumerate(rows):
        kind=row['kind'];yy=row['y'];lines=row['lines']
        if kind=='message':
            n+=1;a=xs[row['a']];b=xs[row['b']];arrowY=yy+row['h']-8
            if a==b:
                pts=[(a,arrowY-7),(a+45,arrowY-7),(a+45,arrowY+7),(a,arrowY+7)]
                tx=a+52;align='left';ly=yy+9
            else:
                end=b-88 if row['create'] and a<b else b+88 if row['create'] else b
                pts=[(a,arrowY),(end,arrowY)];tx=(a+end)/2;align='center';ly=yy+10
            d.arrow(pts,'return' if row['ret'] else 'call')
            label=str(n)+'. '+row['label']
            d.mx_edge(pts,label,'return' if row['ret'] else 'call')
            for j,line in enumerate(lines):
                content=(str(n)+'. ' if j==0 else '')+line
                label_x=tx if align=='center' else tx+stringWidth(content,'Helvetica',10.5)/2
                d.label(label_x,ly+j*14,content,10.5)
        elif kind=='section':
            d.rect(35,yy+2,W-70,row['h']-10,'#edf0f3','#c4cbd2',.6)
            d.text(47,yy+26,lines[0],13,'Helvetica-Bold')
            d.mx_node('section'+str(i),35,yy+2,W-70,row['h']-10,'<b>'+html.escape(lines[0])+'</b>','#edf0f3','spacing=8;')
        elif kind in ('note','ref'):
            x=55;w=W-110;fill='#f6f9fc' if kind=='note' else WHITE
            d.rect(x,yy+5,w,row['h']-12,fill,'#8b96a1',.6)
            offset=50 if kind=='ref' else 12
            if kind=='ref':
                d.rect(x,yy+5,40,22,'#f0f0f0',LINE,.6);d.text(x+7,yy+20,'ref',10,'Helvetica-Bold')
            for j,line in enumerate(lines):d.text(x+offset,yy+23+j*15,line,11)
            d.mx_node('note'+str(i),x,yy+5,w,row['h']-12,'<div style="padding:8px">'+('<b>ref</b> ' if kind=='ref' else '')+html.escape(row['label'])+'</div>',fill)
        else:
            x=100 if kind=='frame' else 60
            for j,line in enumerate(lines):d.text(x,yy+20+j*14,line,11,'Helvetica-Bold')
    d.text(35,H-20,'Verified normal replay: 5 paid / $83.70 / 87.20. Same Stage 3 service priorities without Happy Hour: 5 paid / $86.40 / 86.20. Both complete 18 turns.',11,color=MUTED)
    d.finish()
    (out/'stage3-sequence-model.json').write_text(json.dumps({'participants':PARTICIPANTS,'events':s.events,'frames':s.frames,'rows':rows,'size':[W,H]},indent=2))
    return n,len(s.frames),H


def comparison_report(markdown_path, pdf_path):
    """Readable report exported from the same Markdown kept in the repository."""
    import re
    from reportlab.lib import colors
    from reportlab.lib.enums import TA_LEFT
    from reportlab.lib.pagesizes import A4
    from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
    from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle
    pdf_path.parent.mkdir(parents=True,exist_ok=True)
    width,height=A4;usable=width-84
    styles=getSampleStyleSheet()
    body=ParagraphStyle('Stage3Body',fontName='Helvetica',fontSize=9.2,leading=13.2,spaceAfter=6,textColor=colors.HexColor(INK),splitLongWords=1)
    title=ParagraphStyle('Stage3Title',parent=body,fontName='Helvetica-Bold',fontSize=21,leading=25,spaceAfter=14)
    heading=ParagraphStyle('Stage3Heading',parent=body,fontName='Helvetica-Bold',fontSize=12.2,leading=16,spaceBefore=11,spaceAfter=8,keepWithNext=True)
    cell=ParagraphStyle('Stage3Cell',parent=body,fontSize=8.4,leading=11.6,spaceAfter=0)
    header=ParagraphStyle('Stage3TableHeader',parent=cell,fontName='Helvetica-Bold')
    def inline(s):
        s=html.escape(s.replace('\u2013','-').replace('\u2014','-').replace('\u2011','-'))
        def code(match):
            value=match.group(1)
            if '/' in value and len(value)>38:
                index=value.rfind('/')+1
                value=value[:index]+'<br/>'+value[index:]
            return '<font name="Courier" size="7.4">'+value+'</font>'
        s=re.sub(r'`([^`]+)`',code,s)
        s=re.sub(r'\[([^\]]+)\]\(([^)]+)\)',r'<link href="\2" color="#17558a">\1</link>',s)
        s=re.sub(r'\*\*([^*]+)\*\*',r'<b>\1</b>',s)
        return s
    lines=markdown_path.read_text().splitlines();story=[];i=0
    while i<len(lines):
        line=lines[i].strip()
        if not line:i+=1;continue
        if line.startswith('# '):story.append(Paragraph(inline(line[2:]),title));i+=1;continue
        if line.startswith('## '):story.append(Paragraph(inline(line[3:]),heading));i+=1;continue
        if line.startswith('|'):
            rows=[]
            while i<len(lines) and lines[i].strip().startswith('|'):
                cells=[v.strip() for v in lines[i].strip().strip('|').split('|')]
                if not all(re.fullmatch(r':?-+:?',v.replace(' ','')) for v in cells):rows.append(cells)
                i+=1
            cols=len(rows[0]);fractions={2:[.39,.61],4:[.16,.25,.29,.30],5:[.43,.09,.13,.22,.13]}.get(cols,[1/cols]*cols)
            data=[[Paragraph(inline(v),header if r==0 else cell) for v in row] for r,row in enumerate(rows)]
            table=Table(data,colWidths=[usable*f for f in fractions],repeatRows=1,hAlign='LEFT')
            table.setStyle(TableStyle([('BACKGROUND',(0,0),(-1,0),colors.HexColor('#dcebf8')),('VALIGN',(0,0),(-1,-1),'TOP'),('LEFTPADDING',(0,0),(-1,-1),7),('RIGHTPADDING',(0,0),(-1,-1),7),('TOPPADDING',(0,0),(-1,-1),6),('BOTTOMPADDING',(0,0),(-1,-1),6),('LINEBELOW',(0,0),(-1,0),.7,colors.HexColor('#66717c')),('LINEBELOW',(0,1),(-1,-1),.35,colors.HexColor('#d2d7dc')),('ROWBACKGROUNDS',(0,1),(-1,-1),[colors.white,colors.HexColor('#f6f8fa')])]))
            story.extend([table,Spacer(1,9)]);continue
        bullet=line.startswith('- ')
        parts=[line[2:] if bullet else line];i+=1
        while i<len(lines) and lines[i].strip() and not lines[i].lstrip().startswith(('#','|','- ')):
            parts.append(lines[i].strip());i+=1
        story.append(Paragraph(('&#8226; ' if bullet else '')+inline(' '.join(parts)),body))
    def decorate(c,doc):
        c.saveState()
        c.setStrokeColor(colors.HexColor('#b7c3ce'));c.setLineWidth(.5);c.line(42,height-34,width-42,height-34)
        c.setFont('Helvetica',8);c.setFillColor(colors.HexColor(MUTED));c.drawString(42,height-25,'SC2002 / Restaurant Rush / Stage 2 to Stage 3')
        c.drawString(42,25,'Implementation 3f5e682 | Verified 13 November 2026 | Agent-produced design evidence')
        c.drawRightString(width-42,25,str(doc.page))
        c.restoreState()
    doc=SimpleDocTemplate(str(pdf_path),pagesize=A4,leftMargin=42,rightMargin=42,topMargin=50,bottomMargin=43,title='Restaurant Rush - Stage 2 to Stage 3 change analysis',author='SC2002 Restaurant Rush group')
    doc.build(story,onFirstPage=decorate,onLaterPages=decorate)


def validate_source(repo):
    expected=json.loads((repo/'docs/Stage3/Implementation_Source_Identity.json').read_text())
    for path,sha in expected.items():
        assert hashlib.sha256((repo/path).read_bytes()).hexdigest()==sha,('Source changed; update model before export',path)
    return {path:sha for path,sha in expected.items() if path.startswith('src/')}


def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('--output',type=Path,default=Path(__file__).resolve().parent)
    args=parser.parse_args();repo=Path(__file__).resolve().parents[2]
    args.output.mkdir(parents=True,exist_ok=True)
    source=validate_source(repo)
    print('CLASS_DIAGRAM',class_diagram(args.output))
    print('SEQUENCE_DIAGRAM',sequence_diagram(args.output))
    report=repo/'docs/Stage3/Stage_2_to_3_Changes.pdf'
    comparison_report(repo/'docs/Stage3/Stage_2_to_3_Changes.md',report)
    provenance={'implementation_commit':IMPLEMENTATION,'stage2_commit':STAGE2,'verification_date':'2026-11-13','classes':len(BOXES),'relationships':len(EDGES),'source_sha256':source,'note':'Optional documentation tooling; unchanged Java gameplay. Static keys are underlined; blue headers show Stage 3 additions or modifications.'}
    (args.output/'stage3-diagram-source-identity.json').write_text(json.dumps(provenance,indent=2)+'\n')
    print('CHANGE_REPORT',report)


if __name__=='__main__':
    main()
