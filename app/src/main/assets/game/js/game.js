/*
  page style
*/
// let btngroup = $('.btn_group')
// btngroup.find('p').width(btngroup.width() / 2)
let rechargeDom = $('.recharge')
let withdrawDom = $('.withdraw')
let alertDom = $('.alert')
let loadingDom = $('.loading')
let bodyH = document.body.clientHeight
let rechargeH = rechargeDom.height()
let withdrawH = withdrawDom.height()
rechargeDom.css({'top': (bodyH - rechargeH) / 2})
withdrawDom.css({'top': (bodyH - withdrawH) / 2})
alertDom.css({'top': (bodyH - 60) / 2})
loadingDom.css({'top': bodyH * 0.3})

/*
  config
*/
// QWERasdfZXCV1234
let config = {
  baseurl: 'https://game_api.cybex.io/api/',
  tryoutUrl: '',
  op: {},
  signer: '',
  balance: 0,
  fee_balance: 0,
  scale: null,
  recharge: null
}
let asset = {
  fee: {
    id: '1.3.0',
    asset: 'CYB',
    precision: 5
  },
  amount: {
    id: '1.3.27',
    asset: 'USDT',
    precision: 6
  },
  coin: {
    asset: '金币'
  }
}
let layer = {
  show: function (type) {
    if (type === 'loading' || type === 'alert') {
      $('.layer_whitebg').show()
    } else {
      $('.layer_bg').show()
    }
    $('.' + type).show()
  },
  hide: function (type) {
    if (type === 'loading' || type === 'alert') {
      $('.layer_whitebg').hide()
    } else {
      $('.layer_bg').hide()
    }
    $('.' + type).hide()
  },
  alert: function (str) {
    let alertDom = $('.layer_alert')
    alertDom.html(str)
    layer.show('alert')
    setTimeout(() => {
      alertDom.html('')
      layer.hide('alert')
    }, 3000)
  }
}
/*
  h5_portal with Android & IOS
*/
function potralLogin () {
  let result = Potral.login()
  layer.hide('loading')
  if (result === '') {
    // layer.alert('请先解锁钱包')
  } else {
    loginCallback(result)
  }
}
function loginCallback (result) {
  result = JSON.parse(result)
  config.op = result.op
  config.signer = result.signer
  config.balance = result.balance
  config.fee_balance = result.fee_balance
  $('.layer_balance').html(result.balance + '' + asset.amount.asset)
}
function potralRedirected (url) {
  Potral.redirected(url)
}
function collect (op) {
  // 0 成功
  // 1 上链请求失败 网络问题
  // 2 账户不存在
  Potral.collect(op.account, op.feeAsset, op.asset, op.fee, op.amount)
}
function collectCallback (str) {
  setTimeout(() => {
    layer.hide('recharge')
    layer.hide('loading')
    $('.recharge_amount').val('')
    switch (str) {
    case '0':
      layer.alert('充值成功')
      break
    case '1':
      layer.alert('请求失败')
      break
    case '2':
      layer.alert('充值账户不存在')
      break
    default:
      layer.alert('充值失败')
      break
    }
  }, 5000)
}
function timeout (time = 0) {
  if (config.op.expiration) {
    let currentTime = new Date().getTime()
    if (config.op.expiration * 1000 - currentTime > time) {
      return true
    } else {
      potralLogin()
    }
  } else {
    potralLogin()
  }
}
/*
  serverInterface with 腾海
*/
let serverInterface = {
  getScale: function () {
    return new Promise((resolve, reject) => {
      let result = axios.get(config.baseurl + 'get_scale')
      if (result) {
        resolve(result)
      } else {
        reject()
      }
    })
  },
  getAsset: function () {
    return new Promise((resolve, reject) => {
      let result = axios.get(config.baseurl + 'get_asset')
      if (result) {
        resolve(result)
      } else {
        reject()
      }
    })
  },
  login: function (data) {
    return new Promise((resolve, reject) => {
      let result = axios.post(config.baseurl + 'login', data)
      if (result) {
        resolve(result)
      } else {
        reject()
      }
    })
  },
  getBalance: function (data) {
    return new Promise((resolve, reject) => {
      let result = axios.post(config.baseurl + 'get_balance', data)
      if (result) {
        resolve(result)
      } else {
        reject()
      }
    })
  },
  withdraw: function (data) {
    return new Promise((resolve, reject) => {
      let result = axios.post(config.baseurl + 'withdraw', data)
      if (result) {
        resolve(result)
      } else {
        reject()
      }
    })
  }
}
if (!config.scale) {
  serverInterface.getScale().then((res) => {
    if (res.data.code === 0) {
      config.scale = res.data.data.scale
      let str = '1' + asset.amount.asset + '= ' + res.data.data.scale + ' ' + asset.coin.asset
      $('.recharge_scale').html(str)
      $('.layer_scale').html(str)
      $('.withdraw_layer_input').html(' × ' + res.data.data.scale + ' ' + asset.coin.asset)
    } else {
      layer.alert(res.data.message)
    }
  }).catch(() => {
    layer.alert('网络出错')
  })
}
serverInterface.getAsset().then((res) => {
  if (res.data.code === 0) {
    config.recharge = res.data.data
    $('.layer_fee').html(config.recharge.fee + ' ' + asset.fee.asset)
  } else {
    layer.alert(res.data.message)
  }
}).catch(() => {
  layer.alert('网络出错')
})

/*
  page operation
*/
function getType (dom, type) {
  let str = $(dom).attr(type)
  return str
}
function integer (str, num = 10) {
  str = str.replace(/^(0+)|[^\d]+/g, '')
  if (str.length > num) {
    str = str.substr(0, num)
  }
  return str
}
$('.cancel').click(function () {
  let type = getType($(this), 'type')
  layer.hide(type)
})
$('.confirm').click(function () {
  layer.show('loading')
  if (timeout()) {
    let type = getType($(this), 'type')
    switch (type) {
    case 'recharge':
      let rechargeval = $('.recharge_amount').val()
      if (rechargeval) {
        if (rechargeval > config.balance) {
          layer.hide('loading')
          layer.alert('充值金额大于余额')
        } else if (config.fee_balance < config.recharge.fee) {
          layer.hide('loading')
          layer.alert('手续费不足')
        } else {
          let rechargeOp = {
            account: config.recharge.account, // game-server
            fee: config.recharge.fee * Math.pow(10, asset.fee.precision), // 0.5
            feeAsset: config.recharge.fee_asset, // cyb
            asset: config.recharge.asset, // usdt
            amount: rechargeval * Math.pow(10, asset.amount.precision) // 充值金额
          }
          collect(rechargeOp)
        }
      } else {
        layer.hide('loading')
        layer.alert('充值数额不能为空')
      }
      break
    case 'withdraw':
      let withdrawAmount = $('.withdraw_amount')
      if (withdrawAmount.val()) {
        let withdrawOp = {
          op: config.op,
          signer: config.signer,
          balance: withdrawAmount.val() * config.scale
        }
        serverInterface.withdraw(withdrawOp).then((res) => {
          layer.hide('loading')
          if (res.data.code === 0) {
            layer.hide(type)
            withdrawAmount.val('')
            layer.alert('提现成功')
          } else {
            layer.alert(res.data.message)
          }
        })
      } else {
        layer.hide('loading')
        layer.alert('提现数额不能为空')
      }
      break
    }
  }
})
$('.rewi_btnword').click(function () {
  layer.show('loading')
  if (timeout()) {
    let obj = {
      op: config.op,
      signer: config.signer
    }
    serverInterface.login(obj).then((res) => {
      if (res.data.code === 0) {
        let type = getType($(this), 'type')
        switch (type) {
        case 'recharge':
          if (!config.recharge) {
            serverInterface.getAsset().then((res) => {
              layer.hide('loading')
              if (res.code === 0) {
                config.recharge = res.data.data
                $('.layer_fee').html(config.recharge.fee + ' ' + asset.fee.asset)
                layer.show(type)
              } else {
                layer.alert(res.data.message)
              }
            }).catch(() => {
              layer.hide('loading')
              layer.alert('网络出错')
            })
          } else {
            layer.hide('loading')
            layer.show(type)
          }
          break
        case 'withdraw':
          serverInterface.getBalance(obj).then((res) => {
            layer.hide('loading')
            if (res.data.code === 0) {
              layer.show(type)
              $('.gold_balance').html(res.data.data.balance + ' ' + asset.coin.asset)
            } else {
              layer.alert(res.data.code)
            }
          }).catch(() => {
            layer.hide('loading')
            layer.alert('网络出错')
          })
          break
        }
      } else {
        layer.hide('loading')
        layer.alert(res.data.message)
      }
    }).catch(() => {
      layer.hide('loading')
      layer.alert('网络出错')
    })
  }
})
$('.formal_link').click(function () {
  layer.show('loading')
  if (timeout()) {
    let obj = {
      op: config.op,
      signer: config.signer
    }
    serverInterface.login(obj).then((res) => {
      layer.hide('loading')
      if (res.data.code === 0) {
        potralRedirected(res.data.data.url)
      } else {
        layer.alert(res.data.message)
      }
    }).catch(() => {
      layer.hide('loading')
      layer.alert('网络出错')
    })
  }
})
$('.tryout_link').click(function () {
  potralRedirected(config.tryoutUrl)
})
$('.recharge_amount').keyup(function () {
  let str = integer($(this).val())
  $(this).val(str)
  $('.layer_forecast').html(config.scale * str)
})
$('.withdraw_amount').keyup(function () {
  $(this).val(integer($(this).val()))
})
timeout()
