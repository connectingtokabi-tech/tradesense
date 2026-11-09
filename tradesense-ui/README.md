# TradeSense UI

React + TypeScript dashboard for monitoring processed trades and live validation outcomes.

## Run locally

```bash
npm install
npm run dev
```

Open the Vite URL shown in the terminal. In development, Vite proxies `/api/v1/trades/stream` to the API gateway at `http://localhost:5050`; the gateway forwards it to the trade processor.

The UI defaults to the relative gateway path `/api/v1/trades/stream`. If the UI is hosted on another origin, set `VITE_TRADE_STREAM_URL` in a local `.env` file to the API gateway URL. The stream endpoint must return Server-Sent Events named `trade-saved` with the saved trade and instrumentation payload.

## Build

```bash
npm run build
```
