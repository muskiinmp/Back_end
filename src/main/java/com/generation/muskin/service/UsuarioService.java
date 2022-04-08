package com.generation.muskin.service;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.generation.muskin.model.Usuario;
import com.generation.muskin.model.UsuarioLogin;
import com.generation.muskin.repository.UsuarioRepository;

@Service
public class UsuarioService {
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	public Optional<Usuario> cadastrarUsuario (Usuario usuario) {
		
		if(usuarioRepository.findByUsuario(usuario.getUsuario()).isPresent()) {
			return Optional.empty();
		}
		
		if(calcularIdade(usuario.getDataNascimento()) < 18) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
					"Não permitido porque o usuário é menor de idade", null);
		}
		
		if(usuario.getFoto().isBlank()) {
			usuario.setFoto("https://i.imgur.com/g0CNdx9.png");
		}
		
		usuario.setSenha(criptografarSenha(usuario.getSenha()));
		
		return Optional.ofNullable(usuarioRepository.save(usuario));
	}
	
	public Optional<UsuarioLogin> autenticarUsuario(Optional<UsuarioLogin> usuarioLogin) {
		
		Optional<Usuario> buscarUsuario = usuarioRepository.findByUsuario(usuarioLogin.get().getUsuario());
		
		if(buscarUsuario.isPresent() ) {
			
			if(compararSenhas(usuarioLogin.get().getSenha(), buscarUsuario.get().getSenha() )) {
				
				usuarioLogin.get().setId(buscarUsuario.get().getId());
				usuarioLogin.get().setNome(buscarUsuario.get().getNome());
				usuarioLogin.get().setFoto(buscarUsuario.get().getFoto());
				usuarioLogin.get().setDataNascimento(buscarUsuario.get().getDataNascimento());
				usuarioLogin.get().setToken(gerarBasicToken(usuarioLogin.get().getUsuario(), usuarioLogin.get().getSenha()));
				usuarioLogin.get().setSenha(buscarUsuario.get().getSenha());

				return usuarioLogin;
			}
		}
		
		return Optional.empty();
	}
	
	public Optional<Usuario> atualizarUsuario (Usuario usuario) {
		
		if(usuarioRepository.existsById(usuario.getId())) {
			
			Optional<Usuario> existeUsuario = usuarioRepository.findByUsuario(usuario.getUsuario());
				
			if(existeUsuario.isPresent() && existeUsuario.get().getId() != usuario.getId()) {
				
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esse usuário já existe", null);				
			}
		}
		
		usuario.setSenha(criptografarSenha(usuario.getSenha()));
		
		return Optional.ofNullable(usuarioRepository.save(usuario));
	}
	
	private int calcularIdade(LocalDate dataNascimento) {
		
		return Period.between(dataNascimento, LocalDate.now()).getYears();
	}
	
	private String criptografarSenha(String senha) {
		
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		
		return encoder.encode(senha);
	}
	
	private boolean compararSenhas(String senha, String senhaBanco) {
		
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		
		return encoder.matches(senha, senhaBanco);
	}
	
	private String gerarBasicToken(String usuario, String senha) {
		
		String token = usuario + ":" + senha;
		
		byte[] tokenBase64 = Base64.encodeBase64(token.getBytes(Charset.forName("US-ASCII")));
		
		return "Basic " + new String(tokenBase64);	
	}
}
