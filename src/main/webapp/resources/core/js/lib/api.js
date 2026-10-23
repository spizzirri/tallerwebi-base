/**
 * The one request path for view-layer JavaScript. See docs/guide-js.md R6.
 *
 * Writing `fetch` by hand in each page cost us the same three things every time:
 * the CSRF header spelled out from two <meta> tags, a bare status number where the
 * user needed a sentence, and -- the one that actually bit -- an expired session
 * that answers /api/* with the login page and HTTP 200. The caller then reads
 * HTML as JSON, the parse error reaches the screen, and nothing says "log in".
 */

export class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

export function csrfToken() {
  return document.querySelector('meta[name="_csrf"]')?.content ?? null;
}

function csrfHeaderName() {
  return document.querySelector('meta[name="_csrf_header"]')?.content ?? 'X-CSRF-TOKEN';
}

/** A failed request, as a sentence and a status the caller can branch on. */
export async function apiFetch(url, { method = 'GET', body, headers = {}, signal } = {}) {
  const token = csrfToken();
  const response = await fetch(url, {
    method,
    body,
    signal,
    headers: token ? { [csrfHeaderName()]: token, ...headers } : headers
  });

  // The login page is the tell, not the status: the redirect lands on a 200 whose body
  // is HTML. Keying off the content type also catches a deployment that rewrites the
  // redirect away and leaves the page as a plain 200.
  const contentType = response.headers.get('content-type') || '';
  if (response.redirected || contentType.includes('text/html')) {
    throw new ApiError('Tu sesión venció. Iniciá sesión de nuevo.', 401);
  }

  if (!response.ok) {
    throw new ApiError(await messageOf(response), response.status);
  }

  return response;
}

// The API answers errors as a JSON body with a `message` on a good day, and as anything
// else on a bad one. Either way the status is the only part that is always true.
async function messageOf(response) {
  const detail = await response.text().catch(() => '');
  try {
    const parsed = JSON.parse(detail);
    if (parsed && typeof parsed.message === 'string' && parsed.message) {
      return parsed.message;
    }
  } catch {
    // Not JSON: an error page, a proxy, a stack trace. Fall through to the status.
  }
  return `La operación falló (${response.status}).`;
}
