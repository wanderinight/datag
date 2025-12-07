import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import DataSources from '../views/DataSources.vue'
import DataSets from '../views/DataSets.vue'
import Metadata from '../views/Metadata.vue'
import DataCleaning from '../views/DataCleaning.vue'
import DataQuality from '../views/DataQuality.vue'
import DataLineage from '../views/DataLineage.vue'
import Visualization from '../views/Visualization.vue'
import ChartEditor from '../views/visualization/ChartEditor.vue'
import DashboardBuilder from '../views/visualization/DashboardBuilder.vue'
import DashboardView from '../views/visualization/DashboardView.vue'
import ChartList from '../views/visualization/ChartList.vue'
import DashboardList from '../views/visualization/DashboardList.vue'

const routes = [
  { path: '/', name: 'Home', component: Home },
  { path: '/data-sources', name: 'DataSources', component: DataSources },
  { path: '/data-sets', name: 'DataSets', component: DataSets },
  { path: '/metadata', name: 'Metadata', component: Metadata },
  { path: '/data-cleaning', name: 'DataCleaning', component: DataCleaning },
  { path: '/data-quality', name: 'DataQuality', component: DataQuality },
  { path: '/data-lineage', name: 'DataLineage', component: DataLineage },
  { 
    path: '/visualization', 
    name: 'Visualization', 
    component: Visualization 
  },
  {
    path: '/visualization/charts',
    name: 'ChartList',
    component: ChartList
  },
  {
    path: '/visualization/charts/new',
    name: 'ChartEditor',
    component: ChartEditor
  },
  {
    path: '/visualization/charts/:id/edit',
    name: 'ChartEditorEdit',
    component: ChartEditor
  },
  {
    path: '/visualization/dashboards',
    name: 'DashboardList',
    component: DashboardList
  },
  {
    path: '/visualization/dashboards/new',
    name: 'DashboardBuilder',
    component: DashboardBuilder
  },
  {
    path: '/visualization/dashboards/:id/edit',
    name: 'DashboardBuilderEdit',
    component: DashboardBuilder
  },
  {
    path: '/visualization/dashboards/:id/view',
    name: 'DashboardView',
    component: DashboardView
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
