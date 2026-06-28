const SPORT_TYPES = [
  { value: 'BASKETBALL', label: '篮球' },
  { value: 'FOOTBALL', label: '足球' },
  { value: 'VOLLEYBALL', label: '排球' },
  { value: 'GENERAL', label: '通用赛事' }
];

const STATUS_TEXT = {
  NOT_STARTED: '未开始',
  RUNNING: '进行中',
  PAUSED: '暂停中',
  FINISHED: '已结束'
};

const EVENT_TYPES_BY_SPORT = {
  BASKETBALL: [
    ['SCORE', '得分'],
    ['FOUL', '犯规'],
    ['ASSIST', '助攻'],
    ['REBOUND', '篮板'],
    ['STEAL', '抢断'],
    ['TURNOVER', '失误'],
    ['TIMEOUT', '暂停']
  ],
  FOOTBALL: [
    ['SCORE', '进球'],
    ['FOUL', '犯规'],
    ['YELLOW_CARD', '黄牌'],
    ['RED_CARD', '红牌'],
    ['SUBSTITUTION', '换人']
  ],
  VOLLEYBALL: [
    ['SCORE', '得分'],
    ['BLOCK', '拦网'],
    ['SERVE_ACE', '发球直接得分'],
    ['ERROR', '失误']
  ],
  GENERAL: [
    ['SCORE', '得分'],
    ['FOUL', '犯规']
  ]
};

const EVENT_TYPE_LABELS = {
  SCORE: '得分',
  FOUL: '犯规',
  ASSIST: '助攻',
  REBOUND: '篮板',
  STEAL: '抢断',
  TURNOVER: '失误',
  TIMEOUT: '暂停',
  YELLOW_CARD: '黄牌',
  RED_CARD: '红牌',
  SUBSTITUTION: '换人',
  BLOCK: '拦网',
  SERVE_ACE: '发球直接得分',
  ERROR: '失误'
};
