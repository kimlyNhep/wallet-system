INSERT INTO WLT_EXCHANGE_RATE (ID, DR_CCY, CR_CCY, RATE)
VALUES (1,'USD', 'KHR', '4100')
ON CONFLICT (ID) DO NOTHING;