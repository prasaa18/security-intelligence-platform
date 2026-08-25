const express = require('express');
const axios = require('axios');
const _ = require('lodash');
const moment = require('moment');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json());

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'healthy', timestamp: moment().format() });
});

// Payment processing endpoint (demo)
app.post('/api/payments', async (req, res) => {
  try {
    const paymentData = req.body;
    
    // Demo payment processing logic
    const processedPayment = _.cloneDeep(paymentData);
    processedPayment.processedAt = moment().format();
    processedPayment.status = 'processed';
    
    res.json(processedPayment);
  } catch (error) {
    res.status(500).json({ error: 'Payment processing failed' });
  }
});

// Get payment status (demo)
app.get('/api/payments/:id', (req, res) => {
  const paymentId = req.params.id;
  res.json({
    id: paymentId,
    status: 'completed',
    amount: 100.00,
    currency: 'USD',
    processedAt: moment().format()
  });
});

app.listen(PORT, () => {
  console.log(`Payment service running on port ${PORT}`);
});