package com.generation.muskin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.generation.muskin.model.Usuario;

public interface UsuarioRepository extends JpaRepository <Usuario, Long> {
	
	public Optional <Usuario> findByUsuario(String usuario);
	
	public List <Usuario> findAllByNomeContainingIgnoreCase(String nome);

}
