/**
 * Module 3: Gateway/RBAC section entry page.
 * Function: redirects the former placeholder route to user management.
 * Stack: React 18 + React Router navigation.
 */
import { Navigate } from 'react-router-dom';

export default function GatewayPage() {
  return <Navigate to="/gateway/users" replace />;
}
