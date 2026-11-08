#!/usr/bin/env python3
"""Optional documentation tool: vector PDF/SVG and editable diagrams.net source.
Requires reportlab; never used by the Java build or game. Coordinates are points.
"""
from __future__ import annotations
import argparse, heapq, html, json, math, textwrap
from dataclasses import dataclass
from pathlib import Path
from xml.etree import ElementTree as ET
from reportlab.pdfgen import canvas
from reportlab.pdfbase.pdfmetrics import stringWidth

INK='#20252b'; MUTED='#59616b'; BLUE='#dcebf8'; WHITE='#ffffff'; LINE='#66717c'

class Drawing:
    def __init__(self, path, width, height, title):
        self.width=width; self.height=height; self.path=Path(path)
        self.c=canvas.Canvas(str(self.path.with_suffix('.pdf')),pagesize=(width,height),pageCompression=1)
        self.c.setTitle(title); self.c.setAuthor('SC2002 Restaurant Rush group - Stage 2 working design')
        self.svg=[f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}pt" height="{height}pt" viewBox="0 0 {width} {height}">', '<rect width="100%" height="100%" fill="white"/>']
        self.mx=ET.Element('mxfile',host='app.diagrams.net',version='24.7.17')
        diag=ET.SubElement(self.mx,'diagram',id=self.path.stem,name='Stage 2')
        model=ET.SubElement(diag,'mxGraphModel',dx=str(width),dy=str(height),grid='1',gridSize='10',page='1',pageWidth=str(width),pageHeight=str(height))
        self.root=ET.SubElement(model,'root'); ET.SubElement(self.root,'mxCell',id='0'); ET.SubElement(self.root,'mxCell',id='1',parent='0')
        self.counter=0
    def rect(self,x,y,w,h,fill=WHITE,stroke=INK,lw=.9,dash=False):
        self.c.setFillColor(fill); self.c.setStrokeColor(stroke); self.c.setLineWidth(lw); self.c.setDash([4,3] if dash else [])
        self.c.rect(x,self.height-y-h,w,h,fill=1,stroke=1)
        self.svg.append(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" fill="{fill}" stroke="{stroke}" stroke-width="{lw}"'+(' stroke-dasharray="4 3"' if dash else '')+'/>')
    def line(self,points,color=INK,lw=.9,dash=False):
        self.c.setStrokeColor(color); self.c.setLineWidth(lw); self.c.setDash([5,3] if dash else [])
        p=self.c.beginPath(); p.moveTo(points[0][0],self.height-points[0][1])
        for x,y in points[1:]: p.lineTo(x,self.height-y)
        self.c.drawPath(p)
        pts=' '.join(f'{x:.2f},{y:.2f}' for x,y in points)
        self.svg.append(f'<polyline points="{pts}" fill="none" stroke="{color}" stroke-width="{lw}" stroke-linejoin="round"'+(' stroke-dasharray="5 3"' if dash else '')+'/>')
    def poly(self,points,fill=WHITE,stroke=INK):
        self.c.setFillColor(fill); self.c.setStrokeColor(stroke); self.c.setDash([]); self.c.setLineWidth(1)
        p=self.c.beginPath(); p.moveTo(points[0][0],self.height-points[0][1])
        for x,y in points[1:]:p.lineTo(x,self.height-y)
        p.close(); self.c.drawPath(p,fill=1,stroke=1)
        self.svg.append('<polygon points="'+' '.join(f'{x},{y}' for x,y in points)+f'" fill="{fill}" stroke="{stroke}"/>')
    def text(self,x,y,s,size=10,font='Helvetica',color=INK,align='left',under=False):
        self.c.setFont(font,size); self.c.setFillColor(color)
        width=stringWidth(s,font,size)
        left=x-(width/2 if align=='center' else width if align=='right' else 0)
        self.c.drawString(left,self.height-y,s)
        if under:self.line([(left,y+1.3),(left+width,y+1.3)],color,.5)
        weight='bold' if 'Bold' in font else 'normal'; style='italic' if 'Oblique' in font else 'normal'
        self.svg.append(f'<text x="{left}" y="{y}" font-family="Arial, Helvetica, sans-serif" font-size="{size}" font-weight="{weight}" font-style="{style}" fill="{color}"'+(' text-decoration="underline"' if under else '')+'>'+html.escape(s)+'</text>')
    def label(self,x,y,s,size=9):
        w=stringWidth(s,'Helvetica',size)+6
        self.rect(x-w/2,y-size-2,w,size+5,WHITE,WHITE,0)
        self.text(x,y,s,size,color=MUTED,align='center')
    def mx_node(self,id,x,y,w,h,label,fill=WHITE,style=''):
        c=ET.SubElement(self.root,'mxCell',id=id,value=label,vertex='1',parent='1',style='rounded=0;whiteSpace=wrap;html=1;align=left;verticalAlign=top;spacing=0;overflow=fill;fontFamily=Helvetica;fontSize=10;strokeColor='+INK+';fillColor='+fill+';'+style)
        ET.SubElement(c,'mxGeometry',x=str(x),y=str(y),width=str(w),height=str(h),attrib={'as':'geometry'})
    def mx_edge(self,points,label='',kind='dep',source=None,target=None):
        self.counter+=1
        styles={'dep':'dashed=1;endArrow=open;endFill=0;','assoc':'endArrow=none;','inherit':'endArrow=block;endFill=0;','realize':'dashed=1;endArrow=block;endFill=0;','comp':'startArrow=diamond;startFill=1;endArrow=none;','agg':'startArrow=diamond;startFill=0;endArrow=none;','call':'endArrow=block;endFill=1;','return':'dashed=1;endArrow=open;endFill=0;','lifeline':'dashed=1;endArrow=none;'}
        attr={'id':'edge'+str(self.counter),'value':html.escape(label),'edge':'1','parent':'1','style':'html=1;rounded=0;strokeColor='+LINE+';fontSize=9;'+styles.get(kind,styles['dep'])}
        if source:attr['source']=source
        if target:attr['target']=target
        e=ET.SubElement(self.root,'mxCell',**attr); g=ET.SubElement(e,'mxGeometry',relative='1',attrib={'as':'geometry'})
        ET.SubElement(g,'mxPoint',x=str(points[0][0]),y=str(points[0][1]),attrib={'as':'sourcePoint'})
        ET.SubElement(g,'mxPoint',x=str(points[-1][0]),y=str(points[-1][1]),attrib={'as':'targetPoint'})
        if len(points)>2:
            a=ET.SubElement(g,'Array',attrib={'as':'points'})
            for x,y in points[1:-1]:ET.SubElement(a,'mxPoint',x=str(x),y=str(y))
        if source or target:
            # Fix attachment ports so the editable source follows the exported geometry.
            for id,point,prefix in [(source,points[0],'exit'),(target,points[-1],'entry')]:
                if id and id in BOXES:
                    b=BOXES[id]; f=((point[0]-b.x)/b.w,(point[1]-b.y)/b.h)
                    e.set('style',e.get('style')+f'{prefix}X={f[0]};{prefix}Y={f[1]};{prefix}Dx=0;{prefix}Dy=0;')
    def arrow(self,points,kind='dep'):
        dash=kind in ('dep','realize','return')
        self.line(points,LINE if dash else INK,1,dash)
        def unit(a,b):
            dx=b[0]-a[0];dy=b[1]-a[1];n=math.hypot(dx,dy);return dx/n,dy/n
        if kind in ('comp','agg'):
            x,y=points[0];ux,uy=unit(points[0],points[1]);px,py=-uy,ux
            self.poly([(x,y),(x+7*ux+4*px,y+7*uy+4*py),(x+14*ux,y+14*uy),(x+7*ux-4*px,y+7*uy-4*py)],INK if kind=='comp' else WHITE)
        if kind in ('dep','inherit','realize','call','return'):
            x,y=points[-1];ux,uy=unit(points[-2],points[-1]);px,py=-uy,ux
            tri=[(x,y),(x-11*ux+5*px,y-11*uy+5*py),(x-11*ux-5*px,y-11*uy-5*py)]
            if kind in ('inherit','realize'):self.poly(tri)
            elif kind=='call':self.poly(tri,INK)
            else:self.line([tri[1],tri[0],tri[2]],INK,1)
    def finish(self):
        self.c.showPage(); self.c.save();self.svg.append('</svg>')
        self.path.with_suffix('.svg').write_text('\n'.join(self.svg),encoding='utf-8')
        ET.indent(self.mx); ET.ElementTree(self.mx).write(self.path.with_suffix('.drawio'),encoding='utf-8',xml_declaration=True)

@dataclass
class Box:
    id:str; name:str; stereo:str; x:int; y:int; w:int; attrs:list; ops:list; changed:bool=False; abstract:bool=False
    def __post_init__(self):
        self.alines=self.wrap(self.attrs);self.olines=self.wrap(self.ops)
        self.h=int(math.ceil((45+max(1,len(self.alines))*12+10+max(1,len(self.olines))*12+10)/10)*10)
    def wrap(self,items):
        out=[]
        for entry in items:
            static=entry.startswith('~');entry=entry.lstrip('~')
            words=entry.split(' ');line=''
            for word in words:
                test=(line+' '+word).strip()
                if stringWidth(test,'Helvetica',9.5)>self.w-18 and line:
                    out.append((line,static));line='  '+word
                else:line=test
            if line:out.append((line,static))
        return out
    def draw(self,d):
        d.rect(self.x,self.y,self.w,self.h)
        if self.changed:d.rect(self.x,self.y,self.w,38,BLUE)
        d.text(self.x+self.w/2,self.y+13,self.stereo,9,align='center')
        d.text(self.x+self.w/2,self.y+29,self.name,11,'Helvetica-BoldOblique' if self.abstract else 'Helvetica-Bold',align='center')
        y=self.y+40;d.line([(self.x,y),(self.x+self.w,y)])
        for s,static in self.alines:d.text(self.x+8,y+14,s,9.5,under=static);y+=12
        y=max(y,self.y+52)+8;d.line([(self.x,y),(self.x+self.w,y)])
        for s,static in self.olines:d.text(self.x+8,y+14,s,9.5,under=static);y+=12
        title=html.escape(self.stereo)+'<br/><b>'+html.escape(self.name)+'</b>'
        def block(lines):return '<br/>'.join(('<u>'+html.escape(s)+'</u>') if static else html.escape(s) for s,static in lines) or '&nbsp;'
        label='<div style="text-align:center;padding:4px;background:'+ (BLUE if self.changed else WHITE)+'">'+title+'</div><hr style="margin:0"/><div style="padding:5px">'+block(self.alines)+'</div><hr style="margin:0"/><div style="padding:5px">'+block(self.olines)+'</div>'
        d.mx_node(self.id,self.x,self.y,self.w,self.h,label)

BOXES={}
def add(id,name,stereo,x,y,w,attrs,ops,changed=False,abstract=False):
    b=Box(id,name,stereo,x,y,w,attrs,ops,changed,abstract);BOXES[id]=b;return b

def model():
    BOXES.clear()
    add('Main','Main','<<entry point>>',30,70,220,[],['~+ main(args: String[]): void'],True)
    add('CLI','RestaurantCLI','<<boundary>>',30,260,300,['- in: Scanner {readOnly}','- setupDescription: String {readOnly}'],['+ RestaurantCLI(controller: GameController, in: Scanner)','+ RestaurantCLI(controller: GameController, in: Scanner, setupDescription: String)','+ run(): void','- chooseWaiterAction(view: GameView): void','- chooseChefAction(view: GameView): void','- printState(view: GameView): void','- readInt(prompt: String): int'],True)
    add('Controller','GameController','<<control>>',410,70,350,['- currentTurn: int','- nextCustomerId: int = 1','- phase: TurnPhase','- result: GameResult'],['+ GameController(restaurant: Restaurant, staff: StaffGateway)','+ GameController(restaurant: Restaurant, staff: StaffGateway, customerFactory: IntFunction<? extends Customer>)','+ startTurn(): GameView','+ submitWaiterAction(action: WaiterAction): ActionResult','+ submitChefAction(action: ChefAction): ActionResult','+ finishTurn(): GameView','+ getView(): GameView','+ getLastPaymentResult(): ActionResult'],True)
    add('Factory','CustomerFactory','<<factory>>',840,70,310,['~+ DEFAULT_SEED: long = 24369 {readOnly}','- seed: long {readOnly}','- schedule: Kind[6] {readOnly reference}'],['+ CustomerFactory()','+ CustomerFactory(seed: long)','+ createCustomer(customerId: int): Customer','+ getSeed(): long'],True)
    add('Function','IntFunction<? extends Customer>','<<interface>> {java.util.function}',1230,70,310,[],['+ apply(value: int): Customer'])
    add('Config','GameConfig','<<configuration>>',1620,70,310,['~+ MAX_TURNS: int = 18 {readOnly}','~+ TABLE_COUNT: int = 2 {readOnly}','~+ ARRIVAL_TURNS: Set<Integer> = {1,4,7,10,13,16} {readOnly}','~+ MIN_PAID_CUSTOMERS: int = 4 {readOnly}','~+ MIN_REVENUE_CENTS: int = 4500 {readOnly}','~+ MIN_AVG_SATISFACTION: int = 60 {readOnly}'],[])
    add('ActionResult','ActionResult','<<record>>',2010,70,350,['- accepted: boolean {readOnly}','- message: String {readOnly}'],['~+ success(message: String): ActionResult','~+ rejected(message: String): ActionResult'])
    add('Phase','TurnPhase','<<enumeration>>',410,430,190,['NOT_STARTED','AWAIT_WAITER','AWAIT_CHEF','AUTO','TURN_ENDED','FINISHED'],[])
    add('Result','GameResult','<<enumeration>>',640,430,240,['IN_PROGRESS','VICTORY','DEFEAT'],['~+ evaluate(summary: GameSummary): GameResult'])
    add('Kind','CustomerFactory.Kind','<<private enumeration>>',930,350,220,['Regular','VIP','Critic'],[],True)
    add('Bills','BillCalculator','<<utility>>',1230,350,330,['~- PERCENT_BASE: int = 100 {readOnly}'],['- BillCalculator()','~+ billFor(customer: Customer, item: MenuItem): int','~+ applyDiscount(cents: int, discountPercent: int): int'],True)
    add('Summary','GameSummary','<<record>>',1640,350,290,['- servedCount: int','- paidCount: int','- unhappyDepartures: int','- revenueCents: long','- paidSatisfactionSum: int','- turnsUsed: int'],['+ averagePaidSatisfaction(): OptionalDouble'])
    add('Gateway','StaffGateway','<<interface>>',30,650,320,[],['+ executeWaiter(action: WaiterAction, restaurant: Restaurant, turn: int): ActionResult','+ executeChef(action: ChefAction, restaurant: Restaurant, turn: int): ActionResult','+ processPayment(restaurant: Restaurant, turn: int): ActionResult'])
    add('Team','StaffTeam','<<control>>',30,900,320,[],['+ executeWaiter(action: WaiterAction, restaurant: Restaurant, turn: int): ActionResult','+ executeChef(action: ChefAction, restaurant: Restaurant, turn: int): ActionResult','+ processPayment(restaurant: Restaurant, turn: int): ActionResult'])
    add('Waiter','Waiter','<<entity>>',30,1140,210,['- lastActionTurn: int = 0'],['+ execute(action: WaiterAction, restaurant: Restaurant, turn: int): ActionResult','+ getLastActionTurn(): int'])
    add('Chef','Chef','<<entity>>',290,1140,210,['- lastActionTurn: int = 0'],['+ execute(action: ChefAction, restaurant: Restaurant, turn: int): ActionResult','+ getLastActionTurn(): int'])
    add('Cashier','Cashier','<<entity>>',30,1370,260,['- lastActionTurn: int = 0'],['+ processPayment(restaurant: Restaurant, turn: int): ActionResult','+ getLastActionTurn(): int'])
    add('WA','WaiterAction','<<record>>',350,1370,260,['- type: Type','- customerId: int','- tableId: int','- orderId: int'],['~+ seat(customerId: int, tableId: int): WaiterAction','~+ takeOrder(customerId: int): WaiterAction','~+ serve(orderId: int): WaiterAction','~+ waitTurn(): WaiterAction'])
    add('CA','ChefAction','<<record>>',350,1630,260,['- type: Type','- orderId: int'],['~+ prepare(orderId: int): ChefAction','~+ waitTurn(): ChefAction'])
    add('Restaurant','Restaurant','<<domain coordinator>>',550,680,350,['- currentTurn: int','- completedTurns: int','- lastPenaltyTurn: int','- lastPaymentTurn: int','- nextOrderId: int = 1'],['+ Restaurant(orderFactory: OrderPort.Factory)','+ beginTurn(turn: int): void','+ registerArrival(customer: Customer): void','+ seatCustomer(customerId: int, tableId: int): ActionResult','+ placeOrder(customerId: int): ActionResult','+ advancePreparation(orderId: int, turn: int): ActionResult','+ serveOrder(orderId: int, turn: int): ActionResult','+ advanceEating(turn: int): void','+ findOldestPayableCustomer(turn: int): OptionalInt','+ settlePayment(customerId: int, turn: int): ActionResult','+ applyWaitingPenalty(turn: int): void','+ completeTurn(turn: int): void','+ getSummary(): GameSummary','+ getWaiterOptions(turn: int): List<WaiterAction>','+ getChefOptions(): List<ChefAction>'],True)
    add('Customer','Customer {abstract}','<<entity>>',1010,650,360,['~+ MAX_SATISFACTION: int = 100 {readOnly}','- id: int {readOnly}','- satisfaction: int = 100','- state: CustomerState','- servedTurn: int','- readyToPayTurn: int'],['# Customer(id: int)','# getSatistfactionLossPerTurn(): int {abstract}','+ getTypeName(): String {abstract}','+ chooseItem(menu: Menu): MenuItem','+ getBillDiscountPercent(): int','+ applyWaitingPenalty(): void','+ isOutOfPatience(): boolean','+ seat(): void','+ placeOrder(): void','+ serve(currentTurn: int): void','+ finishedEating(currentTurn: int): boolean','+ pay(): void','+ abandon(): void','- require(expected: CustomerState, action: String): void'],True,True)
    add('State','CustomerState','<<enumeration>>',1450,650,190,['WAITING','SEATED','ORDERED','SERVED','READY_TO_PAY','PAID','LEFT'],['+ isWaitingForFood(): boolean'])
    add('Regular','RegularCustomer','<<entity>>',930,1190,240,['~+ SATISFACTION_LOSS_PER_TURN: int = 8 {readOnly}'],['+ RegularCustomer(id: int)','# getSatistfactionLossPerTurn(): int','+ getTypeName(): String'])
    add('VIP','VipCustomer','<<entity>>',1230,1190,240,['~- LOSS_PER_TURN: int = 5 {readOnly}','~- BILL_DISCOUNT_PERCENT: int = 10 {readOnly}'],['+ VipCustomer(id: int)','# getSatistfactionLossPerTurn(): int','+ getTypeName(): String','+ getBillDiscountPercent(): int'],True)
    add('Critic','CriticCustomer','<<entity>>',1530,1190,240,['~- LOSS_PER_TURN: int = 12 {readOnly}'],['+ CriticCustomer(id: int)','# getSatistfactionLossPerTurn(): int','+ getTypeName(): String','+ chooseItem(menu: Menu): MenuItem'],True)
    add('Seating','SeatingArea','<<entity>>',660,1320,250,[],['+ SeatingArea(tableCount: int)','+ addToQueue(customer: Customer): void','+ isQueued(customer: Customer): boolean','+ seat(customer: Customer): Table','+ seat(customer: Customer, tableId: int): Table','+ removeCustomer(customer: Customer): void','+ getTables(): List<Table>','+ getQueuedCustomers(): List<Customer>'])
    add('Queue','WaitingQueue','<<entity>>',980,1470,250,[],['+ add(customer: Customer): void','+ remove(customer: Customer): boolean','+ contains(customer: Customer): boolean','+ isEmpty(): boolean','+ getCustomers(): List<Customer>'])
    add('Table','Table','<<entity>>',660,1630,250,['- id: int {readOnly}'],['+ Table(id: int)','+ isFree(): boolean','+ seat(customer: Customer): void','+ release(): void','+ getOccupant(): Customer'])
    add('Menu','Menu','<<entity>>',1310,1490,220,[],['+ getItems(): List<MenuItem>'])
    add('Item','MenuItem','<<entity>>',1310,1670,300,['- name: String {readOnly}','- priceCents: int {readOnly}','- prepUnits: int {readOnly}'],['+ MenuItem(name: String, priceCents: int, prepUnits: int)','+ getName(): String','+ getPriceCents(): int','+ prepUnits(): int','+ getPrepUnits(): int','+ getFormattedPrice(): String'],True)
    add('OF','OrderPort.Factory','<<functional interface>>',1740,560,610,[],['+ create(orderId: int, customer: Customer, item: MenuItem, billCents: int): OrderPort'])
    add('Port','OrderPort','<<interface>>',1740,750,330,[],['+ getId(): int','+ getCustomerId(): int','+ getItem(): MenuItem','+ getBillCents(): int','+ getPreparedUnits(): int','+ getStatus(): OrderPort.Status','+ getReadyTurn(): Integer','+ getServedTurn(): Integer','+ getPaidTurn(): Integer','+ getSatisfactionAtPayment(): Integer','+ advanceOneUnit(turn: int): void','+ markServed(turn: int): void','+ markPaid(turn: int, satisfaction: int): void','+ cancel(): void'])
    add('Order','Order','<<entity>>',1830,1320,330,['- id: int {readOnly}','- customerId: int {readOnly}','- billCents: int {readOnly}','- preparedUnits: int','- status: OrderPort.Status','- readyTurn: Integer','- servedTurn: Integer','- paidTurn: Integer','- satisfactionAtPayment: Integer'],['+ Order(id: int, customer: Customer, item: MenuItem, billCents: int)','+ advanceOneUnit(turn: int): void','+ markServed(turn: int): void','+ markPaid(turn: int, satisfaction: int): void','+ cancel(): void'])
    add('Status','OrderPort.Status','<<enumeration>>',2210,1320,180,['PLACED','PREPARING','READY','SERVED','PAID','CANCELLED'],[])
    add('View','GameView','<<immutable record>>',2130,750,260,['- turn: int','- phase: TurnPhase','- waitingCustomerIds: List<Integer>','- customers: List<CustomerView>','- tables: List<TableView>','- menu: List<MenuItemView>','- orders: List<OrderView>','- staff: StaffView','- summary: GameSummary','- result: GameResult'],['+ GameView(...) {List.copyOf}'])
    add('WT','WaiterAction.Type','<<enumeration>>',30,1690,250,['SEAT','TAKE_ORDER','SERVE','WAIT'],[])
    add('CT','ChefAction.Type','<<enumeration>>',350,1830,260,['PREPARE','WAIT'],[])

# (source, target, kind, role/label, source multiplicity, target multiplicity)
EDGES=[
 ('Main','CLI','dep','<<instantiate>>','',''),('Main','Controller','dep','<<instantiate>>','',''),('Main','Factory','dep','<<instantiate>>','',''),
 ('Main','Function','dep','<<bind>> customers::createCustomer','',''),('CLI','Controller','assoc','-controller','','1'),
 ('Controller','Function','assoc','-customerFactory','','1'),('Controller','Restaurant','assoc','-restaurant','','1'),('Controller','Gateway','assoc','-staff','','1'),
 ('Controller','Phase','assoc','-phase','','1'),('Controller','Result','assoc','-result','','1'),('Controller','Config','dep','<<use>>','',''),('Controller','View','dep','<<return>>','',''),
 ('Factory','Kind','assoc','-schedule','','6'),('Factory','Customer','dep','<<create>>','',''),('Function','Customer','dep','<<return>>','',''),
 ('Team','Gateway','realize','','',''),('Team','Waiter','comp','-waiter','1','1'),('Team','Chef','comp','-chef','1','1'),('Team','Cashier','comp','-cashier','1','1'),
 ('Waiter','Restaurant','dep','<<use>>','',''),('Chef','Restaurant','dep','<<use>>','',''),('Cashier','Restaurant','dep','<<use>>','',''),
 ('Waiter','WA','dep','<<use>>','',''),('Chef','CA','dep','<<use>>','',''),('WA','WT','assoc','-type','','1'),('CA','CT','assoc','-type','','1'),
 ('Restaurant','Customer','agg','-customers','1','0..*'),('Restaurant','Port','comp','-orders','1','0..*'),('Restaurant','OF','assoc','-orderFactory','','1'),
 ('Restaurant','Seating','comp','-seatingArea','1','1'),('Restaurant','Menu','comp','-menu','1','1'),('Restaurant','Bills','dep','<<use>>','',''),
 ('Bills','Customer','dep','discount dispatch','',''),('Bills','Item','dep','price in cents','',''),
 ('Regular','Customer','inherit','','',''),('VIP','Customer','inherit','','',''),('Critic','Customer','inherit','','',''),('Customer','State','assoc','-state','','1'),
 ('Critic','Item','dep','longest prep units','',''),('Customer','Menu','dep','<<use>>','',''),
 ('Seating','Table','comp','-tables','1','2'),('Seating','Queue','comp','-queue','1','1'),('Queue','Customer','agg','-customers {ordered}','0..1','0..*'),
 ('Table','Customer','assoc','-occupant','0..1','0..1'),('Menu','Item','comp','-items','1','3'),('Order','Port','realize','','',''),
 ('Order','Item','assoc','-item','0..*','1'),('Order','Status','assoc','-status','','1'),('Port','Status','dep','<<use>>','',''),('OF','Port','dep','<<create>>','',''),
 ('View','Summary','assoc','-summary','','1'),('Result','Summary','dep','<<use>>','','')]

class Router:
    def __init__(self,w,h,obstacles):
        self.w=int(w/10);self.h=int(h/10);self.blocked=set();self.usage={};self.ports={}
        for x,y,bw,bh in obstacles:
            for gx in range(max(1,int((x-8)//10)),min(self.w,int(math.ceil((x+bw+8)/10)))+1):
                for gy in range(max(7,int((y-8)//10)),min(self.h,int(math.ceil((y+bh+8)/10)))+1):self.blocked.add((gx,gy))
    def candidates(self,b):
        result=[]
        for side in ['L','R','T','B']:
            for f in [.25,.5,.75]:
                if side in ['L','R']:
                    y=round((b.y+b.h*f)/10)*10;x=b.x if side=='L' else b.x+b.w
                    outer=(int(x/10)+(-2 if side=='L' else 2),int(y/10));p=(x,y);direction=0
                else:
                    x=round((b.x+b.w*f)/10)*10;y=b.y if side=='T' else b.y+b.h
                    outer=(int(x/10),int(y/10)+(-2 if side=='T' else 2));p=(x,y);direction=1
                if outer not in self.blocked and 1<=outer[0]<self.w and 7<=outer[1]<self.h:
                    result.append((outer,p,direction))
        return result
    def route(self,a,b):
        starts=self.candidates(a);ends=self.candidates(b);goals={x[0]:(x[1],x[2]) for x in ends}
        if not starts or not ends:raise ValueError('No ports '+a.id+' '+b.id)
        def h(p):return min(abs(p[0]-g[0])+abs(p[1]-g[1]) for g in goals)
        q=[];dist={};prev={};origin={};serial=0
        for g,p,d in starts:
            s=(*g,d);cost=self.ports.get((a.id,p),0)*12
            dist[s]=cost;origin[s]=p;heapq.heappush(q,(cost+h(g),serial,s));serial+=1
        finish=None
        while q:
            _,_,s=heapq.heappop(q);x,y,d=s
            if (x,y) in goals:finish=s;break
            for dx,dy,nd in [(1,0,0),(-1,0,0),(0,1,1),(0,-1,1)]:
                n=(x+dx,y+dy,nd);g=n[:2]
                if g in self.blocked or not(1<=g[0]<self.w and 7<=g[1]<self.h):continue
                cost=dist[s]+1+(2 if nd!=d else 0)+self.usage.get(g,0)*3
                if cost<dist.get(n,1e30):
                    dist[n]=cost;prev[n]=s;origin[n]=origin[s];heapq.heappush(q,(cost+h(g),serial,n));serial+=1
        if finish is None:raise ValueError('Cannot route '+a.id+' '+b.id)
        cells=[];s=finish
        while s in prev:cells.append(s[:2]);s=prev[s]
        cells.append(s[:2]);cells.reverse()
        start=origin[finish];end=goals[finish[:2]][0]
        points=[start]+[(x*10,y*10) for x,y in cells]+[end]
        points=[p for i,p in enumerate(points) if i==0 or p!=points[i-1]]
        compact=[points[0]]
        for i in range(1,len(points)-1):
            a0=compact[-1];p=points[i];n=points[i+1]
            if (a0[0]==p[0]==n[0]) or (a0[1]==p[1]==n[1]):continue
            compact.append(p)
        compact.append(points[-1])
        for cell in cells:self.usage[cell]=self.usage.get(cell,0)+1
        self.ports[(a.id,start)]=self.ports.get((a.id,start),0)+1
        self.ports[(b.id,end)]=self.ports.get((b.id,end),0)+1
        return compact

def note(d,id,x,y,w,lines,title=None):
    wrapped=[]
    for s in lines:
        wrapped+=textwrap.wrap(s,max(20,int(w/5.2))) or ['']
    h=24+len(wrapped)*12+(16 if title else 0)
    d.rect(x,y,w,h,'#fafafa',LINE,.7)
    yy=y+17
    if title:d.text(x+8,yy,title,10,'Helvetica-Bold');yy+=17
    for s in wrapped:d.text(x+8,yy,s,9.5);yy+=12
    d.mx_node(id,x,y,w,h,'<div style="padding:8px">'+('<b>'+html.escape(title)+'</b><br/>' if title else '')+'<br/>'.join(html.escape(s) for s in wrapped)+'</div>','#fafafa')
    return (x,y,w,h)

def class_diagram(out):
    model();W=2520;H=2020
    for box in BOXES.values():box.x+=50
    for a in BOXES.values():
        assert a.x>=20 and a.y>=60 and a.x+a.w<W-10 and a.y+a.h<H-60,(a.id,a.h)
        for b in BOXES.values():
            if a.id<b.id:
                assert not(a.x<b.x+b.w+8 and b.x<a.x+a.w+8 and a.y<b.y+b.h+8 and b.y<a.y+a.h+8),('overlap',a.id,b.id,a.h,b.h)
    d=Drawing(out/'Stage2_UML_Diagram',W,H,'Restaurant Rush - Stage 2 Class Diagram')
    d.text(30,30,'Restaurant Rush - Stage 2 Class Diagram',20,'Helvetica-Bold')
    d.text(30,49,'Local lichen/stage_2 working code | Stage 1 layout conventions retained | Added / changed classes have a blue header',11,color=MUTED)
    extras=[]
    extras.append(note(d,'Legend',2010,310,350,['+ public; - private; # protected. Underlined = static.','Hollow triangle = inheritance; dashed triangle = realization.','Filled diamond = composition; hollow diamond = aggregation.','Dashed open arrow = dependency. Numbers are multiplicities.','Reference-valued fields may be represented by association roles. Only key operations are shown.'],'UML notation'))
    extras.append(note(d,'DTOs',2270,1540,180,['Nested immutable records:','CustomerView','TableView','MenuItemView','OrderView','StaffView','Lists copied with List.copyOf; record accessors are implicit.'],'GameView records'))
    extras.append(note(d,'OO',30,1870,260,['Customer state is private and changed through guarded operations.','Loss, food choice and discount dispatch through Customer methods.','StaffTeam owns staff; SeatingArea owns two Tables and a WaitingQueue.'],'OO examples'))
    extras.append(note(d,'FunctionNote',1710,1830,740,['Main passes customers::createCustomer as an IntFunction. CustomerFactory does not implement IntFunction, so there is no realization arrow between them. The controller stores the function, not a concrete CustomerFactory.','Default seed 24369: Regular, VIP, Regular, Regular, Regular, Critic. Six arrivals: 1, 4, 7, 10, 13, 16. Critic chooses the longest preparation time; ties use menu order. VIP discount is locked into the order bill.'],'Stage 2 dispatch and reproducibility'))
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
    d.text(30,H-20,'Reference: Stage1_UML_Diagram.pdf. Stage 2 source names and signatures checked 8 November 2026; no Stage 3 features.',10,color=MUTED)
    d.finish()
    (out/'stage2-class-layout.json').write_text(json.dumps({'boxes':[{**b.__dict__} for b in BOXES.values()],'routes':routes},indent=2))
    return len(BOXES),len(EDGES)


class Sequence:
    def __init__(self):
        self.events=[];self.frames=[];self.active=[]
    def msg(self,a,b,label,ret=False,create=False):self.events.append({'kind':'message','a':a,'b':b,'label':label,'ret':ret,'create':create})
    def note(self,label):self.events.append({'kind':'note','label':label})
    def ref(self,label):self.events.append({'kind':'ref','label':label})
    def section(self,label):self.events.append({'kind':'section','label':label})
    def start(self,op,label):
        f={'op':op,'label':label,'start':len(self.events),'end':None,'dividers':[]};self.frames.append(f);self.active.append(f)
        self.events.append({'kind':'frame','label':label,'op':op})
    def otherwise(self,label):
        self.active[-1]['dividers'].append((len(self.events),label));self.events.append({'kind':'divider','label':label})
    def end(self):self.active.pop()['end']=len(self.events)

PARTICIPANTS=[
 ('M','Manager','<<actor>>'),('CLI','cli:RestaurantCLI','<<boundary>>'),('C','game:GameController','<<control>>'),
 ('Fn','factoryFn:IntFunction','<<Java method reference>>'),('F','customers:CustomerFactory','<<factory>>'),
 ('S','staff:StaffTeam','<<StaffGateway>>'),('W','waiter:Waiter','<<entity>>'),('Ch','chef:Chef','<<entity>>'),
 ('Ca','cashier:Cashier','<<entity>>'),('R','restaurant:Restaurant','<<domain>>'),
 ('V','vip:VipCustomer','<<created on turn 4>>'),('B','BillCalculator','<<static utility>>'),
 ('OFn','orderFn:OrderPort.Factory','<<Order::new>>'),('O','order2:Order','<<created on turn 5>>'),('A','seating:SeatingArea','<<entity>>'),('G','GameResult','<<enumeration>>')]

def sequence_model():
    s=Sequence();m=s.msg
    s.note('Starting conditions: default seed 24369; entering turn 4 of stage2-victory.txt. Customer #1 was served on turn 3 and occupies Table 1; Table 2 is free. VIP #2 has not arrived yet. r = restaurant; results are returned as ActionResult / GameView.')
    s.section('TURN 4 - arrival and seating')
    m('CLI','C','startTurn()')
    m('C','Fn','apply(2) [arrival turn]')
    m('Fn','F','createCustomer(2)')
    m('F','V','VipCustomer(2)',create=True)
    m('F','Fn','vip {WAITING, sat=100}',ret=True)
    m('Fn','C','vip',ret=True)
    m('C','R','beginTurn(4)')
    m('C','R','registerArrival(vip)')
    m('R','A','addToQueue(vip)')
    m('C','CLI','GameView {AWAIT_WAITER}',ret=True)
    m('M','CLI','choose SEAT #2 at Table 2')
    m('CLI','C','submitWaiterAction(SEAT(2,2))')
    m('C','S','executeWaiter(SEAT(2,2),r,4)')
    m('S','W','execute(SEAT(2,2),r,4)')
    m('W','R','seatCustomer(2,2)')
    m('R','A','seat(vip,2)')
    m('A','V','seat() -> SEATED')
    s.ref('Successful ActionResult returns through Waiter / StaffTeam / GameController; Waiter lastActionTurn=4 and phase=AWAIT_CHEF. CLI selects Chef WAIT through submitChefAction -> StaffTeam.executeChef -> Chef.execute; phase becomes AUTO. Queue/table mutation occurs inside SeatingArea.seat().')
    m('CLI','C','finishTurn()')
    m('C','R','advanceEating(4)')
    m('C','S','processPayment(r,4)')
    m('S','Ca','processPayment(r,4)')
    m('Ca','R','findOldestPayableCustomer(4)')
    m('R','Ca','empty [no readyToPayTurn < 4]',ret=True)
    m('C','R','applyWaitingPenalty(4)')
    m('R','V','applyWaitingPenalty()')
    m('V','V','getSatistfactionLossPerTurn() = 5')
    s.note('VIP satisfaction becomes 95. Customer #1 becomes READY_TO_PAY on turn 4 but cannot pay until turn 5. The same finishTurn ordering is used on every turn: eating -> Cashier -> penalty -> completeTurn -> result evaluation.')
    m('C','R','completeTurn(4)')
    m('C','G','evaluate(summary4)')
    m('G','C','IN_PROGRESS [turnsUsed < 18]',ret=True)
    m('C','CLI','GameView {TURN_ENDED}',ret=True)
    s.section('TURN 5 - manager choice and actual VIP order')
    m('CLI','C','startTurn()')
    m('C','R','beginTurn(5)')
    s.start('alt','[manager takes VIP order now; main branch follows the recorded victory priority rule]')
    m('M','CLI','choose TAKE_ORDER #2')
    m('CLI','C','submitWaiterAction(TAKE_ORDER(2))')
    m('C','S','executeWaiter(TAKE_ORDER(2),r,5)')
    m('S','W','execute(TAKE_ORDER(2),r,5)')
    m('W','R','placeOrder(2)')
    m('R','V','chooseItem(menu)')
    m('V','R','Burger {base price=1500 cents}',ret=True)
    m('R','B','billFor(vip,Burger)')
    m('B','V','getBillDiscountPercent()')
    m('V','B','10',ret=True)
    m('B','B','applyDiscount(1500,10) = 1350')
    m('B','R','1350 cents',ret=True)
    m('R','OFn','create(2,vip,Burger,1350)')
    m('OFn','O','Order(2,vip,Burger,1350)',create=True)
    m('OFn','R','order2 {PLACED; billCents=1350}',ret=True)
    m('R','V','placeOrder() -> ORDERED')
    s.ref('OrderPort.Factory.create(2,vip,Burger,1350) is bound to Order::new. Restaurant validates the fresh order, stores it, and returns success. The bill is final on the Order. Successful Waiter execution records its allowance; controller phase becomes AWAIT_CHEF. Invalid input/action would retry without advancing phase or using allowance.')
    m('CLI','C','getView() [fresh Chef options]')
    m('M','CLI','choose PREPARE Order 2')
    m('CLI','C','submitChefAction(PREPARE(2))')
    m('C','S','executeChef(PREPARE(2),r,5)')
    m('S','Ch','execute(PREPARE(2),r,5)')
    m('Ch','R','advancePreparation(2,5)')
    m('R','O','advanceOneUnit(5)')
    s.note('Order 2 becomes READY with preparedUnits=1 and readyTurn=5. The Waiter has already acted and cannot serve again this turn. Chef success returns through StaffTeam; phase becomes AUTO.')
    m('CLI','C','finishTurn()')
    m('C','R','advanceEating(5)')
    s.ref('Automatic Cashier chain as on turn 4: customer #1 is now eligible and pays $9.00; Table 1 is released. Those Order 1 / Customer 1 messages are outside this VIP lifeline and are intentionally collapsed here.')
    m('C','R','applyWaitingPenalty(5)')
    m('R','V','applyWaitingPenalty() -> sat=90')
    s.ref('completeTurn(5); GameResult.evaluate(summary5) returns IN_PROGRESS. The order being READY does not stop waiting loss: VIP is still ORDERED until served.')
    s.section('TURN 6 - serve the dish on a later turn')
    m('CLI','C','startTurn()')
    m('C','R','beginTurn(6)')
    m('M','CLI','choose SERVE Order 2')
    m('CLI','C','submitWaiterAction(SERVE(2))')
    m('C','S','executeWaiter(SERVE(2),r,6)')
    m('S','W','execute(SERVE(2),r,6)')
    m('W','R','serveOrder(2,6)')
    m('R','O','markServed(6) [READY; readyTurn 5 < 6]')
    m('R','V','serve(6) -> SERVED')
    s.ref('Waiter success; Chef WAIT; CLI calls finishTurn(). Staff capacity is still one task / preparation unit per turn.')
    m('C','R','advanceEating(6)')
    m('R','V','finishedEating(6)')
    m('V','R','false [6 is not > servedTurn 6]',ret=True)
    s.ref('Cashier finds no eligible VIP. Waiting penalty leaves a SERVED customer unchanged; completeTurn(6) and result evaluation follow. VIP satisfaction stays 90.')
    s.section('TURN 7 - one full turn of eating; no immediate payment')
    s.ref('startTurn() on turn 7 also creates Regular customer #3. From the recorded policy: Waiter seats #3; Chef waits. Those non-VIP messages are collapsed. CLI then calls finishTurn().')
    m('C','R','advanceEating(7)')
    m('R','V','finishedEating(7) [7 > servedTurn 6]')
    m('V','R','true; READY_TO_PAY; readyToPayTurn=7',ret=True)
    m('C','S','processPayment(r,7)')
    m('S','Ca','processPayment(r,7)')
    m('Ca','R','findOldestPayableCustomer(7)')
    m('R','Ca','empty [VIP readyToPayTurn 7 is not < 7]',ret=True)
    s.ref('Penalty applies only to unserved customers: VIP remains at 90. completeTurn(7); evaluate -> IN_PROGRESS.')
    s.section('TURN 8 - automatic payment, snapshot and table release')
    s.ref('startTurn() on turn 8; recorded staff choices take Regular #3 order and prepare its first Pasta unit. CLI then calls finishTurn().')
    m('C','R','advanceEating(8)')
    m('C','S','processPayment(r,8)')
    m('S','Ca','processPayment(r,8)')
    m('Ca','R','findOldestPayableCustomer(8)')
    m('R','Ca','OptionalInt(2) [7 < 8; oldest eligible]',ret=True)
    m('Ca','R','settlePayment(2,8)')
    m('R','O','markPaid(8,90) -> PAID')
    m('R','V','pay() -> PAID')
    m('R','A','removeCustomer(vip)')
    s.note('SeatingArea calls Table 2.release(). Order 2 retains billCents=1350, paidTurn=8 and satisfactionAtPayment=90. Revenue is computed from PAID orders; unpaid/abandoned orders are excluded.')
    m('R','Ca','success [one payment this turn]',ret=True)
    m('Ca','S','success; lastActionTurn=8',ret=True)
    m('S','C','success',ret=True)
    m('C','R','applyWaitingPenalty(8)')
    m('C','R','completeTurn(8)')
    m('C','G','evaluate(summary8)')
    m('G','C','IN_PROGRESS [only 8 turns used]',ret=True)
    m('C','CLI','GameView {paid=2, revenue=$22.50, avg=87.00}',ret=True)
    s.section('TURNS 9-18 - complete the game; same victory/defeat criteria')
    s.start('loop','[remaining turns 9..18; no early ending]')
    s.ref('For every turn: arrivals on 10/13/16; one Waiter choice, then one Chef choice; advanceEating -> automatic oldest payment -> per-customer penalty / abandonment -> completeTurn. All other-customer interactions follow stage2-victory.txt.')
    m('C','G','evaluate(restaurant.getSummary())')
    s.start('alt','[turnsUsed < 18]')
    m('G','C','IN_PROGRESS',ret=True)
    s.otherwise('[turnsUsed = 18]')
    m('G','C','VICTORY [paid=5; 6450 >= 4500; 402 >= 60*5]',ret=True)
    s.end();s.end()
    m('C','CLI','GameView {FINISHED, VICTORY}',ret=True)
    s.note('TAKE_ORDER branch final outcome: 18/18 turns; 5 paid; $64.50; average paid satisfaction 80.40; zero unhappy departures. CLI prints the final result. Critic #6 cannot pay before the deadline, as intended.')
    s.otherwise('[manager chooses WAIT on turn 5; same priority rule resumes on turn 6]')
    m('M','CLI','choose WAIT instead of TAKE_ORDER #2')
    s.ref('Separate counterfactual replay: VIP orders/prepares on turn 6, is served on turn 7, becomes READY_TO_PAY on turn 8 and pays on turn 9. Its payment snapshot is 85. The remaining customers are also scheduled by the same priority rule. Final result: VICTORY, 5 paid, $64.50, satisfaction sum 389, average 77.80. Thus one staff-priority choice changes timing and satisfaction, while both runs still win.')
    s.end()
    s.note('Scope: one complex Stage 2 use case with a separately replayed alternative. ref frames abbreviate existing interactions; they do not add methods to the code. OptionalInt/getSummary return details and pure GameView construction are abbreviated. Parameters r and action factories are shorthand for actual Restaurant / action records.')
    return s

def sequence_diagram(out):
    s=sequence_model();W=3220;xs={p[0]:80+i*200 for i,p in enumerate(PARTICIPANTS)}
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
    H=y+75;d=Drawing(out/'Stage2_VIP_Sequence_Diagram',W,H,'Restaurant Rush - Stage 2 VIP sequence and manager choice')
    d.text(35,32,'Restaurant Rush - Stage 2: a manager choice changes VIP service timing and paid satisfaction',21,'Helvetica-Bold')
    d.text(35,54,'Concrete default-seed replay, turns 4-18 | Solid arrow: synchronous call; dashed arrow: return | ref: abbreviated actual subinteraction',11,color=MUTED)
    d.text(35,73,'Named objects and method calls follow current lichen/stage_2 source. The alternative is a separate production-code replay, not a claim that it causes defeat.',11,color=MUTED)
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
            d.rect(x-88,top,176,46,BLUE if id in ('Fn','F','V','B') else WHITE,INK,.9)
            d.text(x,top+15,stereo,9,align='center');d.text(x,top+32,name,10,'Helvetica-Bold',align='center')
            d.mx_node('participant'+id,x-88,top,176,46,'<div style="text-align:center;padding:4px">'+html.escape(stereo)+'<br/><b>'+html.escape(name)+'</b></div>',BLUE if id in ('Fn','F','V','B') else WHITE)
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
    d.text(35,H-20,'Verified normal replay: 5 paid / $64.50 / 80.40. Single WAIT-at-turn-5 alternative: 5 paid / $64.50 / 77.80. Both use the same seed and 18-turn objectives.',11,color=MUTED)
    d.finish()
    (out/'stage2-sequence-model.json').write_text(json.dumps({'participants':PARTICIPANTS,'events':s.events,'frames':s.frames,'rows':rows,'size':[W,H]},indent=2))
    return n,len(s.frames),H

if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--output',type=Path,default=Path(__file__).resolve().parent)
    args=parser.parse_args();args.output.mkdir(parents=True,exist_ok=True)
    print('CLASS_DIAGRAM',class_diagram(args.output))
    print('SEQUENCE_DIAGRAM',sequence_diagram(args.output))
