"""
Seeds demo transaction history for EquiBalance by pulling historical daily closes
from Yahoo Finance (via yfinance, no API key required) and inserting synthetic
BUY transactions into an existing portfolio.

This is the "Python (optional exposure)" piece from the PRD (section 2 / 8) - it
is a standalone script, not part of the running application, meant to make demo
portfolios look realistic when presenting the project.

Usage:
    python seed_prices.py --portfolio-id 1 --tickers AAPL,VTI,BND --days 90

Requires:
    - DATABASE_URL env var (or .env), e.g.
      postgresql://equibalance:equibalance@localhost:5432/equibalance
    - The portfolio and its holdings must already exist (created via the API/UI)
      so this script only needs to know which portfolio_id to attach transactions to.
"""

import argparse
import os
import sys
from datetime import datetime

import psycopg2
import yfinance as yf
from dotenv import load_dotenv

load_dotenv()


def parse_args():
    parser = argparse.ArgumentParser(description="Seed EquiBalance demo transaction history")
    parser.add_argument("--portfolio-id", type=int, required=True)
    parser.add_argument("--tickers", type=str, required=True, help="Comma-separated tickers, e.g. AAPL,VTI,BND")
    parser.add_argument("--days", type=int, default=90, help="How many days of history to pull")
    parser.add_argument("--units-per-buy", type=float, default=1.0)
    return parser.parse_args()


def get_connection():
    database_url = os.environ.get("DATABASE_URL", "postgresql://equibalance:equibalance@localhost:5432/equibalance")
    return psycopg2.connect(database_url)


def seed_ticker(conn, portfolio_id: int, ticker: str, days: int, units_per_buy: float):
    history = yf.Ticker(ticker).history(period=f"{days}d")
    if history.empty:
        print(f"  no data returned for {ticker}, skipping")
        return 0

    inserted = 0
    with conn.cursor() as cur:
        cur.execute(
            "SELECT id FROM holdings WHERE portfolio_id = %s AND ticker = %s",
            (portfolio_id, ticker),
        )
        row = cur.fetchone()
        holding_id = row[0] if row else None

        # Sample every 5th trading day to keep the seed dataset small but visible on a chart.
        for i, (date, row_data) in enumerate(history.iterrows()):
            if i % 5 != 0:
                continue
            close_price = float(row_data["Close"])
            executed_at = date.to_pydatetime()

            cur.execute(
                """
                INSERT INTO transactions (portfolio_id, holding_id, ticker, type, units, price_at_time, executed_at)
                VALUES (%s, %s, %s, 'BUY', %s, %s, %s)
                """,
                (portfolio_id, holding_id, ticker, units_per_buy, close_price, executed_at),
            )
            inserted += 1

    conn.commit()
    return inserted


def main():
    args = parse_args()
    tickers = [t.strip().upper() for t in args.tickers.split(",") if t.strip()]

    conn = get_connection()
    try:
        total = 0
        for ticker in tickers:
            print(f"Seeding {ticker}...")
            total += seed_ticker(conn, args.portfolio_id, ticker, args.days, args.units_per_buy)
        print(f"Done. Inserted {total} synthetic transactions at {datetime.now().isoformat()}")
    finally:
        conn.close()


if __name__ == "__main__":
    sys.exit(main())
