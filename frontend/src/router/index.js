import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import AttractionView from '../views/AttractionView.vue'
import OrdersView from '../views/OrdersView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/attractions/:id', name: 'attraction', component: AttractionView, props: true },
    { path: '/orders', name: 'orders', component: OrdersView },
    { path: '/:pathMatch(.*)*', redirect: '/' }
  ]
})

export default router
