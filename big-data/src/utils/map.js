import { jsonp } from 'vue-jsonp'

const MapService = {
  BdBrowserAk: 'HnXK4K8PMqq4ksyA6R8Tr7fdhTD9Gut3',

  // 返回地点的城市信息
  // 是否召回传入坐标周边的poi，0为不召回，1为召回。当值为1时，默认显示周r边1000米内的poi. 注意：若需召回国外POI，需单独申请权限
  geoCoder(callbackName, latitude, longitude, poi) {
    jsonp(`https://api.map.baidu.com/geocoder/v2/?callback=${callbackName}&output=json&pois=1`, {
      ak: 'HnXK4K8PMqq4ksyA6R8Tr7fdhTD9Gut3',
      location: latitude + ',' + longitude,
    }).then((res) => {
      console.log(res)
    })
  },
  getMap: (query, fn) => {
    jsonp(`https://api.map.baidu.com/place/v2/suggestion?output=json`, {
      ak: 'HnXK4K8PMqq4ksyA6R8Tr7fdhTD9Gut3',
      region: ' ',
      query
    }).then(res => {
      fn(res)
    })
  }
}

export default MapService
