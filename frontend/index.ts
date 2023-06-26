import { Router } from '@vaadin/router';
import { routes } from './routes.js';
import { color, typography } from "@vaadin/vaadin-lumo-styles/all-imports.js";

const style = document.createElement("style");
style.innerHTML = `${color.toString()} ${typography.toString()}`;

document.head.appendChild(style);

export const router = new Router(document.querySelector('#outlet'));
router.setRoutes(routes);
