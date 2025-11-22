import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Counter, Trend } from 'k6/metrics';
import { SharedArray } from 'k6/data';

// Métricas customizadas
const errorRate = new Rate('errors');
const notificationCounter = new Counter('notifications_sent');
const notificationDuration = new Trend('notification_duration');

// Token de autenticação (será preenchido no setup)
let authToken = '';

// Configuração do teste
export const options = {
  stages: [
    { duration: '30s', target: 10 },   // Ramp-up para 50 VUs em 30s
    { duration: '1m', target: 20 },   // Ramp-up para 100 VUs em 1min
    { duration: '2m', target: 20 },   // Mantém 100 VUs por 2min
    { duration: '30s', target: 40 },  // Ramp-up para 200 VUs em 30s
    { duration: '2m', target: 40 },   // Mantém 200 VUs por 2min
    { duration: '30s', target: 0 },    // Ramp-down
  ],
  thresholds: {
    'http_req_duration': ['p(95)<3000'], // 95% das requisições devem ser < 3s
    'errors': ['rate<0.1'],               // Taxa de erro < 10%
  },
};

// Função de setup - executada uma vez antes do teste
export function setup() {
  console.log('Authenticating...');

  const loginUrl = 'http://localhost:8081/api/auth/login';
  const loginPayload = JSON.stringify({
    clientId: 'demo-client',
    password: 'demo123'
  });

  const loginParams = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const loginResponse = http.post(loginUrl, loginPayload, loginParams);

  if (loginResponse.status !== 200) {
    console.error(`Login failed with status ${loginResponse.status}: ${loginResponse.body}`);
    throw new Error('Authentication failed. Cannot proceed with test.');
  }

  const loginData = JSON.parse(loginResponse.body);
  const token = loginData.token;

  if (!token) {
    console.error('No token received in login response');
    throw new Error('Authentication failed. No token received.');
  }

  console.log('Authentication successful! Token obtained.');

  return { token: token };
}

// Dados para geração aleatória
const channels = ['SMS', 'PUSH', 'EMAIL'];

const phoneNumbers = [
  '5511999999999',
  '5521988888888',
  '5531977777777',
  '5541966666666',
  '5551955555555',
  '5561944444444',
];

const emailAddresses = [
  'user1@example.com',
  'user2@example.com',
  'test@demo.com',
  'customer@company.com',
  'admin@platform.com',
  'support@service.com',
];

const pushTokens = [
  'token_AAAAbbbbCCCCdddd1111',
  'token_EEEEffffGGGGhhhh2222',
  'token_IIIIjjjjKKKKllll3333',
  'token_MMMMnnnnOOOOpppp4444',
  'token_QQQQrrrrSSSSttttt5555',
];

const emailSubjects = [
  'Welcome to our platform!',
  'Your order has been confirmed',
  'Password reset request',
  'New message received',
  'Account verification',
  'Special offer just for you!',
];

const emailContents = [
  'Hello! This is a test email notification with important information.',
  'Thank you for your purchase. Your order will be delivered soon.',
  'You requested a password reset. Click the link to proceed.',
  'You have received a new message. Log in to view it.',
  'Please verify your account by clicking the verification link.',
  'Don\'t miss our exclusive offer! Limited time only.',
];

const smsContents = [
  'Your verification code is: 123456',
  'Hello! Your order has been shipped.',
  'Reminder: Your appointment is tomorrow at 10am',
  'Your payment was successfully processed',
  'Welcome! Thanks for joining us.',
];

const pushContents = [
  'Hello! This is a test push notification.',
  'You have a new notification waiting for you.',
  'Breaking news: Check out what\'s new!',
  'Your request has been completed successfully.',
  'Reminder: Don\'t forget to check your messages.',
];

const priorities = ['LOW', 'MEDIUM', 'HIGH'];

// Função para gerar número aleatório
function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

// Função para selecionar elemento aleatório de um array
function randomChoice(array) {
  return array[randomInt(0, array.length - 1)];
}

// Função para gerar recipient baseado no canal
function generateRecipient(channel) {
  switch (channel) {
    case 'SMS':
      return randomChoice(phoneNumbers);
    case 'EMAIL':
      return randomChoice(emailAddresses);
    case 'PUSH':
      return randomChoice(pushTokens);
    default:
      return 'default@example.com';
  }
}

// Função para gerar subject baseado no canal
function generateSubject(channel) {
  if (channel === 'EMAIL') {
    return randomChoice(emailSubjects);
  }
  return null; // SMS e PUSH não usam subject
}

// Função para gerar content baseado no canal
function generateContent(channel) {
  switch (channel) {
    case 'SMS':
      return randomChoice(smsContents);
    case 'EMAIL':
      return randomChoice(emailContents);
    case 'PUSH':
      return randomChoice(pushContents);
    default:
      return 'Default notification content';
  }
}

// Função para gerar payload de notificação
function generateNotificationPayload() {
  const channel = randomChoice(channels);
  const timestamp = Date.now();
  const randomId = randomInt(100000, 999999);

  const payload = {
    idempotencyKey: `perf-test-${channel.toLowerCase()}-${timestamp}-${randomId}`,
    clientId: 'demo-client',
    channel: channel,
    recipient: generateRecipient(channel),
    content: generateContent(channel),
    priority: randomChoice(priorities),
    maxRetries: randomInt(1, 5),
  };

  // Sempre adiciona subject (obrigatório pela API)
  const subject = generateSubject(channel);
  payload.subject = subject || `Notification via ${channel}`;

  return payload;
}

// Função principal executada por cada VU (Virtual User)
export default function (data) {
  const url = 'http://localhost:8081/api/notifications/notifications/send';

  const payload = generateNotificationPayload();

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${data.token}`,
    },
    tags: {
      channel: payload.channel,
    },
  };

  const startTime = Date.now();
  const response = http.post(url, JSON.stringify(payload), params);
  const duration = Date.now() - startTime;

  // Registra métricas
  notificationDuration.add(duration);
  notificationCounter.add(1);

  // Valida resposta
  const success = check(response, {
    'status is 200 or 201': (r) => r.status === 200 || r.status === 201,
    'response has body': (r) => r.body.length > 0,
    'response time < 5s': (r) => r.timings.duration < 5000,
  });

  if (!success) {
    errorRate.add(1);
    console.log(`Error: ${response.status} - ${response.body}`);
  } else {
    errorRate.add(0);
  }

  // Pequena pausa entre requisições (ajuste conforme necessário)
  sleep(0.1); // 100ms de pausa = ~10 req/s por VU
}

// Função executada ao final do teste
export function handleSummary(data) {
  return {
    'stdout': textSummary(data, { indent: ' ', enableColors: true }),
    'performance-test-results.json': JSON.stringify(data),
  };
}

function textSummary(data, options) {
  const indent = options.indent || '';
  const enableColors = options.enableColors || false;

  let summary = '\n' + indent + '========================================\n';
  summary += indent + 'Performance Test Summary\n';
  summary += indent + '========================================\n\n';

  summary += indent + `Total Requests: ${data.metrics.http_reqs?.values.count || 0}\n`;
  summary += indent + `Failed Requests: ${data.metrics.http_req_failed?.values.passes || 0}\n`;
  summary += indent + `Avg Response Time: ${(data.metrics.http_req_duration?.values.avg || 0).toFixed(2)}ms\n`;
  summary += indent + `P95 Response Time: ${(data.metrics.http_req_duration?.values['p(95)'] || 0).toFixed(2)}ms\n`;
  summary += indent + `P99 Response Time: ${(data.metrics.http_req_duration?.values['p(99)'] || 0).toFixed(2)}ms\n`;
  summary += indent + `Error Rate: ${((data.metrics.errors?.values.rate || 0) * 100).toFixed(2)}%\n`;
  summary += indent + `Requests per second: ${(data.metrics.http_reqs?.values.rate || 0).toFixed(2)}\n\n`;

  return summary;
}

