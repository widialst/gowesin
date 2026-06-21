-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: May 29, 2026 at 01:03 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `gowesin`
--

-- --------------------------------------------------------

--
-- Table structure for table `bikes`
--

CREATE TABLE `bikes` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `price_per_hour` decimal(10,2) NOT NULL,
  `location` varchar(100) DEFAULT NULL,
  `battery` int(11) DEFAULT 100,
  `rating` decimal(2,1) DEFAULT 5.0,
  `image_url` varchar(255) DEFAULT NULL,
  `status` enum('available','rented') DEFAULT 'available'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `bikes`
--

INSERT INTO `bikes` (`id`, `name`, `description`, `price_per_hour`, `location`, `battery`, `rating`, `image_url`, `status`) VALUES
(1, 'Gowes Pro X1', 'Sepeda listrik kencang dengan ketahanan baterai hingga 50km.', 15000.00, 'Stasiun Tugu', 95, 4.8, 'https://images.unsplash.com/photo-1571068316344-75bc76f77894?q=80&w=500', 'rented'),
(2, 'City Roller E2', 'Sangat cocok untuk berkeliling kota dengan santai.', 10000.00, 'Malioboro', 80, 4.5, 'https://images.unsplash.com/photo-1593133630449-b7b28f1ed35c?q=80&w=500', 'rented'),
(3, 'Mountain E-Bike', 'Ban tebal, cocok untuk jalanan tidak rata.', 20000.00, 'Alun-alun Kidul', 100, 4.9, 'https://images.unsplash.com/photo-1621067510443-417646698661?q=80&w=500', 'available');

-- --------------------------------------------------------

--
-- Table structure for table `rentals`
--

CREATE TABLE `rentals` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `bike_id` int(11) NOT NULL,
  `rental_date` date NOT NULL,
  `duration` int(11) NOT NULL,
  `total_price` decimal(10,2) NOT NULL,
  `payment_method` varchar(50) DEFAULT NULL,
  `status` enum('renting','finished','canceled') DEFAULT 'renting',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `start_time` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `rentals`
--

INSERT INTO `rentals` (`id`, `user_id`, `bike_id`, `rental_date`, `duration`, `total_price`, `payment_method`, `status`, `created_at`, `start_time`) VALUES
(1, 3, 1, '2026-05-16', 1, 15000.00, 'E-Wallet (OVO/Dana/Gopay)', '', '2026-05-16 14:54:43', '2026-05-16 22:02:43'),
(2, 3, 2, '2026-05-16', 1, 10000.00, 'Cash', '', '2026-05-16 14:59:59', '2026-05-16 22:02:43');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `role` enum('user','admin') DEFAULT 'user',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `name`, `email`, `password`, `phone`, `role`, `created_at`) VALUES
(1, 'Admin Gowes', 'admin@gowes.in', '$2y$10$8Wk6p5L/8n0X5O8Q6.6pGe6C.M8XWfI6G1e8/1Q2m3n4o5p6q7r8s', '08123456789', 'admin', '2026-05-16 13:51:20'),
(2, 'Budi Santoso', 'budi@gmail.com', '$2y$10$8Wk6p5L/8n0X5O8Q6.6pGe6C.M8XWfI6G1e8/1Q2m3n4o5p6q7r8s', '08987654321', 'user', '2026-05-16 13:51:20'),
(3, 'Widia Sulistiani', 'widiasulistiani12@gmail.com', '$2y$10$iduo3DJzZ1GlZTW06zXudu821VwP1aQQmaAi1udn83bjs0Xnb7.hW', '085780674184', 'user', '2026-05-16 14:30:29');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `bikes`
--
ALTER TABLE `bikes`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `rentals`
--
ALTER TABLE `rentals`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `bike_id` (`bike_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `bikes`
--
ALTER TABLE `bikes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `rentals`
--
ALTER TABLE `rentals`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `rentals`
--
ALTER TABLE `rentals`
  ADD CONSTRAINT `rentals_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `rentals_ibfk_2` FOREIGN KEY (`bike_id`) REFERENCES `bikes` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
