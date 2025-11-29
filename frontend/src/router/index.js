import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import DataSources from '../views/DataSources.vue'
import DataSets from '../views/DataSets.vue'
import Metadata from '../views/Metadata.vue'
import DataCleaning from '../views/DataCleaning.vue'
import DataQuality from '../views/DataQuality.vue'
import DataLineage from '../views/DataLineage.vue'
import Visualization from '../views/Visualization.vue'

const routes = [
  { path: '/', name: 'Home', component: Home },
  { path: '/data-sources', name: 'DataSources', component: DataSources },
  { path: '/data-sets', name: 'DataSets', component: DataSets },
  { path: '/metadata', name: 'Metadata', component: Metadata },
  { path: '/data-cleaning', name: 'DataCleaning', component: DataCleaning },
  { path: '/data-quality', name: 'DataQuality', component: DataQuality },
  { path: '/data-lineage', name: 'DataLineage', component: DataLineage },
  { path: '/visualization', name: 'Visualization', component: Visualization }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

