const DEFAULT_LOCALE = navigator.language || 'en-GB'

/** Display-only formatting. Amounts are never computed on in the browser; the backend owns the arithmetic. */
export function formatMoney(amount: number | string, currency: string, locale = DEFAULT_LOCALE): string {
  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency,
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(Number(amount))
}

export function formatDate(isoDate: string, locale = DEFAULT_LOCALE): string {
  // ISO dates (yyyy-mm-dd) are calendar dates: format them in UTC so they never shift by a day.
  return new Intl.DateTimeFormat(locale, { day: '2-digit', month: 'short', year: 'numeric', timeZone: 'UTC' }).format(
    new Date(`${isoDate.slice(0, 10)}T00:00:00Z`),
  )
}
