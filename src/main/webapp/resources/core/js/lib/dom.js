/**
 * Hyperscript, not a framework: the smallest thing that builds a node without handing a
 * string to `innerHTML`. See docs/guide-js.md R7.
 *
 *   el('div', { class: 'x' }, [el('strong', { text: place.name })])
 *
 * `text` sets `textContent` -- the only way a value becomes text. Every other key becomes an
 * attribute, so `class`, `href`, `aria-label` and `data-*` are written exactly as they read
 * in HTML. `null`/`undefined` props are skipped, because a conditional attribute is usually
 * a conditional value.
 *
 * A child that is not a Node becomes a text node, so `[place.name]` and
 * `[el('strong', { text: place.name })]` both work and neither parses markup.
 * `null`/`undefined` children are skipped too, so `cond ? el('br') : null` is safe.
 */

export function el(tag, props = {}, children = []) {
  const node = document.createElement(tag);
  for (const [key, value] of Object.entries(props)) {
    if (value === null || value === undefined) continue;
    if (key === 'text') node.textContent = value;
    else if (key === 'dataset') Object.assign(node.dataset, value);
    else node.setAttribute(key, value);
  }
  for (const child of [].concat(children)) {
    if (child === null || child === undefined) continue;
    node.append(child instanceof Node ? child : document.createTextNode(child));
  }
  return node;
}
