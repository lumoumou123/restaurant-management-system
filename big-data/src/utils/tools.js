// 深拷贝
export function deepClone(obj, newObj) {
  var newObj = newObj || {};
  for (let key in obj) {
    if (typeof obj[key] == 'object') {
      let isArray = Array.isArray(obj[key]);//判断是否数组
      newObj[key] = (isArray == true ? [] : {})
      deepClone(obj[key], newObj[key]);
    } else {
      newObj[key] = obj[key]
    }
  }
  return newObj;
}
// 保留两位小数
export function toDecimal2(num) {
  if(num/10000 <= 1) return num
  let f = num/10000
  f = Math.floor(f * 100) / 100
  var s = f.toString()
  var rs = s.indexOf('.')
  if (rs < 0) {
    rs = s.length
    s += '.'
  }
  while (s.length <= rs + 2) {
    s += '0'
  }
  const result = parseFloat(s)
  return s
}

export default {
  deepClone,
  toDecimal2
}
