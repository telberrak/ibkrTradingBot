use ibkr_trading_dev;

drop table orders;
drop table bars;
drop table positions;
drop table contracts;

create table contracts
(
	conId bigint unsigned not null primary key,
    tickerId bigint unsigned not null,
    symbol varchar(30) not null,
    secType varchar(15) not null,
    conExchange varchar(30) not null default 'SMART',
    primaryExchange varchar(30) not null,
    currency varchar(3) not null,
    strike float null,
    lastTradedateOrContractMonth varchar(8) null,
    optRight varchar(10) null,
    multiplier double null,
    secId varchar(30) null,
    secIdType varchar(30) null,
    updateDate datetime not null ,
    
    unique key unique_contract (symbol,secType,primaryExchange)
 );

 
 CREATE TABLE orders (
    orderId bigint unsigned not null primary key AUTO_INCREMENT,
    tickerId bigint unsigned not null references contracts(tickerId),
    orderAction varchar(10) not null,
    quantity integer unsigned not null,
    orderType varchar(10) not null default 'LMT',
    price double not null,
    timeInForce varchar(10) not null default 'DAY',
    orderStatus varchar(30) not null, 
    commission double null
);
 
 /*OrderStatus  ApiPending,
	ApiCancelled,
	PreSubmitted,
	PendingCancel,
	Cancelled,
	Submitted,
	Filled,
	Inactive,
	PendingSubmit,
	Unknown;*/
    
    
    create table bars
    (
    barId bigint unsigned  not null primary key AUTO_INCREMENT,
    createdOn datetime not null default now(),
    tickerId bigint unsigned not null references contracts(tickerId), 
    barTime varchar(30) not null,
    barOpen double not null,
    barHigh double not null,
    barLow double not null,
    barClose double not null,
    barVolume bigint unsigned not null,
    barCount integer null,
    barWap double null, 
    timeframe varchar(30)
    );

create table positions
(
	posId bigint unsigned not null primary key AUTO_INCREMENT,
    tickerId bigint unsigned not null references contracts(tickerId),
    accountId varchar(30) not null,
    position double not null,
    marketPrice double not null,
    marketValue double not null,
    avgCost double not null,
    unrealPnl double not null,
    realPnl double not null
);

alter table positions add foreign key (tickerId) references contracts(tickerId);
alter table bars add foreign key (tickerId) references contracts(tickerId);
alter table orders add foreign key (tickerId) references contracts(tickerId);