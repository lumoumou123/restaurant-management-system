// import Vue from 'vue'
// import Vuex from 'vuex'
// import api from '@/api'
import { Loading } from 'element-ui'
// Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    loadingInstance: '',
  },
  mutations: {
    FullLoading(state, payload) {
      // state.loadingInstance = Loading.service({ fullscreen: true })
    }
  },
  actions: {
    CloseLoading({ commit, state }, payload) {
      state.loadingInstance.close && state.loadingInstance.close()
    },
    FullLoading({ commit }, payload) {
      commit('FullLoading')
    }
  }
})
