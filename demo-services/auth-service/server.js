const express = require('express');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcrypt');
const cookieParser = require('cookie-parser');

const app = express();
const PORT = process.env.PORT || 3002;

app.use(express.json());
app.use(cookieParser());

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'healthy', service: 'auth-service' });
});

// Login endpoint (demo)
app.post('/api/auth/login', async (req, res) => {
  try {
    const { username, password } = req.body;
    
    // Demo authentication logic
    if (username && password) {
      const token = jwt.sign(
        { username, role: 'user' },
        'demo-secret-key',
        { expiresIn: '1h' }
      );
      
      res.json({
        success: true,
        token: token,
        user: { username, role: 'user' }
      });
    } else {
      res.status(400).json({ error: 'Username and password required' });
    }
  } catch (error) {
    res.status(500).json({ error: 'Authentication failed' });
  }
});

// Verify token endpoint (demo)
app.get('/api/auth/verify', (req, res) => {
  const token = req.headers.authorization?.replace('Bearer ', '');
  
  if (token) {
    try {
      const decoded = jwt.verify(token, 'demo-secret-key');
      res.json({ valid: true, user: decoded });
    } catch (error) {
      res.status(401).json({ valid: false, error: 'Invalid token' });
    }
  } else {
    res.status(401).json({ valid: false, error: 'No token provided' });
  }
});

// Hash password endpoint (demo)
app.post('/api/auth/hash', async (req, res) => {
  const { password } = req.body;
  if (password) {
    const saltRounds = 10;
    const hash = await bcrypt.hash(password, saltRounds);
    res.json({ hash });
  } else {
    res.status(400).json({ error: 'Password is required' });
  }
});

app.listen(PORT, () => {
  console.log(`Auth service running on port ${PORT}`);
});