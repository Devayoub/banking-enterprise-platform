#!/usr/bin/env bash
# Simulates the legacy core banking system: puts one booked-transaction message on the core MQ queue.
# The Camel route (banking-integration-camel) should translate it and publish it to Kafka, and the core
# service should book it on the demo customer's current account.
#
#   ./docker/scripts/send-sample-core-message.sh            # XML credit of 125.50 EUR
#   ./docker/scripts/send-sample-core-message.sh invalid    # malformed message, ends on the DLQ
set -euo pipefail

QUEUE="CORE.BANKING.TRANSACTIONS.OUT"
MQ_WEB_USER="${MQ_WEB_USER:-admin}"
MQ_WEB_PASSWORD="${MQ_WEB_PASSWORD:-admin}"
REF="CORE-$(date +%Y%m%d)-$RANDOM"
TODAY="$(date +%Y-%m-%d)"

if [[ "${1:-}" == "invalid" ]]; then
  BODY="<CoreTransaction><TxnRef>${REF}</TxnRef><Amount>not-a-number</Amount></CoreTransaction>"
else
  BODY="<CoreTransaction>
  <TxnRef>${REF}</TxnRef>
  <Iban>FR7630006000011234567890189</Iban>
  <DrCr>C</DrCr>
  <Amount>125.50</Amount>
  <Ccy>EUR</Ccy>
  <BookingDate>${TODAY}</BookingDate>
  <ValueDate>${TODAY}</ValueDate>
  <Narrative>Card refund</Narrative>
</CoreTransaction>"
fi

curl --fail --silent --show-error -u "${MQ_WEB_USER}:${MQ_WEB_PASSWORD}" \
  --data-urlencode "body=${BODY}" \
  "http://localhost:8161/api/message/${QUEUE}?type=queue"
echo "Sent ${REF} to ${QUEUE}"
