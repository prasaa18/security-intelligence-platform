const express = require('express');
const request = require('request');
const forge = require('node-forge');

const app = express();
const PORT = process.env.PORT || 3001;

app.use(express.json());

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'healthy', service: 'order-service' });
});

// Order processing endpoint (demo)
app.post('/api/orders', (req, res) => {
  try {
    const orderData = req.body;
    
    // Demo order processing logic
    const processedOrder = {
      ...orderData,
      orderId: 'ORD-' + Date.now(),
      status: 'processing',
      createdAt: new Date().toISOString()
    };
    
    res.json(processedOrder);
  } catch (error) {
    res.status(500).json({ error: 'Order processing failed' });
  }
});

// Get order status (demo)
app.get('/api/orders/:id', (req, res) => {
  const orderId = req.params.id;
  res.json({
    id: orderId,
    status: 'completed',
    items: ['item1', 'item2'],
    total: 250.00,
    createdAt: new Date().toISOString()
  });
});

// Demo encryption endpoint (for testing)
app.post('/api/encrypt', (req, res) => {
  const { text } = req.body;
  if (text) {
    const md = forge.md.md5.create();
    md.update(text);
    const hash = md.digest().toHex();
    res.json({ hash });
  } else {
    res.status(400).json({ error: 'Text is required' });
  }
});

app.listen(PORT, () => {
  console.log(`Order service running on port ${PORT}`);
});