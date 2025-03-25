import * as echarts from 'echarts'
const config = {
  // 饼状图配置
  pieChart: {
    color: [ '#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de',
      '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc',
      '#2ec7c9','#b6a2de','#5ab1ef','#ffb980','#d87a80',
      '#8d98b3','#e5cf0d','#97b552','#95706d','#dc69aa'],
    tooltip: {
      trigger: 'item',
      formatter: '{a} : ({d}%)'
    },
    legend: {
      // left: 0,
      // bottom: 20,
      type: 'scroll',
      // orient: 'vertical',
      left: 'center',
      bottom: 10,
      textStyle: {
        fontSize: 10,
        color: 'rgba(97, 137, 183, 1)'
      },
      pageIconColor: 'rgba(97, 137, 183, 1)'
    },
    label: {
      normal: {
        show: true
      },
      emphasis: {
        show: true,
        textStyle: {
            fontSize: 10,
            fontWeight: 'bold',
        },
      }
    },
    series: [
      {
        type: 'pie',
        radius: [20, 40],
        // radius: [40, 80],
        // center: ['60%', '45%'],
        center: ['50%', '40%'],
        // roseType: 'area',
        itemStyle: {
          borderRadius: 1
        },
        label: {
          normal: {
            textStyle: {
              color: 'white'
            }
          },
          position:'center',
        },
        labelLine:{
          show: true,
          length: 2, //调整上下位置
        },
        data: []
      }
    ]
  },
  // 柱状图
  categoryChart: {
    grid:{ // 让图表占满容器
      top:"20px",
      left:"50px",
      right:"15px",
      bottom:"40px"
    },
    tooltip: {
      trigger: 'item',
      // formatter: "{b}:({d}%)"
    },
    xAxis: {
      type: 'category',
      // x轴数据
      data: [],
      axisTick: { // x轴刻度线
        show: false
      },
      axisLine: {
        lineStyle: {
          opacity: 0
        },
      },
      axisLabel: {
        color: 'white'
      }
    },
    yAxis: {
      type: 'value',
      splitLine: { show: false }, // 去除网格线
      axisLabel: {
        color: 'white'
      },
      minInterval: 1
      // min: 1
    },
    series: [
      {
        data: [],// y轴数据
        type: 'bar',
        itemStyle: {
          color: 'rgba(68, 174, 236, 1)',
          borderRadius: [100, 100, 100, 100], //（顺时针左上，右上，右下，左下）
        },
        barMaxWidth: "25%",
        label: {
          show: true,
          position: 'top',
          color: 'white'
        }
      },

    ],
    dataZoom: [
      {
        type: 'inside', // 内置在坐标系中
        // start:0, // 数据窗口范围的起始百分比。范围是：0 ~ 100。表示 0% ~ 100%。
        // end:  100, // 数据窗口范围的结束百分比。范围是：0 ~ 100。
        startValue: 0, // 数据窗口范围的起始数值。如果设置了 dataZoom-inside.start 则 startValue 失效。
        endValue: 6, // 数据窗口范围的结束数值。如果设置了 dataZoom-inside.end 则 endValue 失效。
        zoomLock: true, // 是否锁定选择区域（或叫做数据窗口）的大小。
      }
    ]
  },
  // 折线图
  lineChart: {
    minInterval: 1, // 需要为正整数
    tooltip: {
      trigger: 'axis'
    },
    grid:{ // 让图表占满容器
      top:"20px",
      left:"50px",
      right:"15px",
      bottom:"40px"
    },
    xAxis: {
      type: 'category',
      boundaryGap: true,
      data: [],
      axisLine: {
        lineStyle: { color: '#44AEEC' } // x轴坐标轴颜色
      },
      axisTick: { // x轴刻度线
        show: false
      }
    },
    yAxis: {
      type: 'value',
      splitLine: { show: false }, // 去除网格线
      axisLabel: {
        color: 'white'
      }
    },
    series: [{
      // symbol: 'none',
      data: [],
      type: 'line',
      // smooth: true,
      lineStyle: {
        color: '#44AEEC' // 改变折线颜色
      },
      label: {
        show: true,
        position: 'top',
        color: 'white'
      },
      // areaStyle: {
      //   normal: {
      //     // 颜色渐变函数 前四个参数分别表示四个位置依次为左、下、右、上
      //     color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
      //       offset: 0,
      //       color: 'rgba(68,174,236,0.9)'
      //     }, {
      //       offset: 0.5,
      //       color: 'rgba(68,174,236,0.3)'
      //     }, {
      //       offset: 1,
      //       color: 'rgba(68,174,236,0.1)'
      //     }])
      //   }
      // } // 区域颜色渐变
    }]
  }
}
export default config
