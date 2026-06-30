const MATCH_STATUS = {
  NOT_STARTED: { label: '未开始', badge: 'badge-gray' },
  RUNNING: { label: '进行中', badge: 'badge-green' },
  PAUSED: { label: '已暂停', badge: 'badge-orange' },
  FINISHED: { label: '已结束', badge: 'badge-blue' }
};

const SPORT_TYPE = {
  BASKETBALL: { label: '篮球', icon: '🏀' },
  FOOTBALL: { label: '足球', icon: '⚽' },
  VOLLEYBALL: { label: '排球', icon: '🏐' },
  GENERAL: { label: '通用', icon: '🏅' }
};

const EVENT_TYPE = {
  SCORE: { label: '得分', badge: 'badge-green' },
  FOUL: { label: '犯规', badge: 'badge-red' },
  ASSIST: { label: '助攻', badge: 'badge-blue' },
  REBOUND: { label: '篮板', badge: 'badge-orange' },
  STEAL: { label: '抢断', badge: 'badge-blue' },
  TURNOVER: { label: '失误', badge: 'badge-red' },
  TIMEOUT: { label: '暂停', badge: 'badge-orange' },
  YELLOW_CARD: { label: '黄牌', badge: 'badge-orange' },
  RED_CARD: { label: '红牌', badge: 'badge-red' },
  SUBSTITUTION: { label: '换人', badge: 'badge-gray' },
  BLOCK: { label: '拦网', badge: 'badge-blue' },
  SERVE_ACE: { label: '发球得分', badge: 'badge-green' },
  ERROR: { label: '失误', badge: 'badge-red' }
};

const STAT_KEY = {
  SCORE: '得分',
  ASSIST: '助攻',
  REBOUND: '篮板',
  FOUL: '犯规',
  STEAL: '抢断',
  TURNOVER: '失误'
};

const POSITION_OPTIONS = ['PG', 'SG', 'SF', 'PF', 'C', 'GK', 'DEF', 'MID', 'FWD', '自由人', '二传', '主攻', '副攻', '接应'];
